package fr.bnia.authentification.application;

import fr.bnia.authentification.domain.CompteConnexion;
import fr.bnia.authentification.domain.EvenementConnexion;
import fr.bnia.authentification.domain.Politique;
import fr.bnia.authentification.domain.Secrets;
import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Mots de passe (spec 03 §5.1) : premier mot de passe après l'acceptation d'une invitation (RG-AUT-15),
 * changement depuis « Mon compte » (RG-AUT-07), mot de passe oublié (RG-AUT-08).
 */
@UseCase
public class GererMotDePasse {

    private final CompteRepository comptes;
    private final HachageMotDePasse hachage;
    private final OperateursLecture operateurs;
    private final SessionsOuvertes sessions;
    private final Politique politique;
    private final Greffe greffe;

    public GererMotDePasse(CompteRepository comptes, HachageMotDePasse hachage, OperateursLecture operateurs,
            SessionsOuvertes sessions, JournalConnexions journal, EnvoiCourriels envoi,
            ParametresAuthentification parametres, Clock horloge) {
        this.comptes = comptes;
        this.hachage = hachage;
        this.operateurs = operateurs;
        this.sessions = sessions;
        this.politique = parametres.politique();
        this.greffe = new Greffe(journal, envoi, parametres, horloge);
    }

    /**
     * RG-AUT-07, 12, 15 : avec le jeton de l'invitation qu'il vient d'accepter, le nouvel utilisateur pose
     * son mot de passe, si son compte n'en a pas ; la session s'ouvre sans code, l'e-mail venant d'être
     * prouvé.
     *
     * @return l'utilisateur, pour ouvrir sa session
     */
    public long definirPremierMotDePasse(String jetonInvitation, String motDePasse, Contexte contexte) {
        CompteConnexion.verifierMotDePasse(motDePasse, politique);
        long utilisateur = operateurs.utilisateurDInvitationAcceptee(jetonInvitation).orElseThrow(() -> new RegleEnfreinte(
                "RG-AUT-15", "Ce lien ne permet plus de définir un mot de passe : utilisez « mot de passe oublié »."));
        CompteConnexion compte = comptes.parUtilisateur(utilisateur).orElseThrow(() -> new ElementIntrouvable(
                "Aucun compte pour cet utilisateur."));
        if (compte.aUnMotDePasse()) {
            throw new RegleEnfreinte("RG-AUT-15", "Votre mot de passe est déjà défini : connectez-vous, ou utilisez"
                    + " « mot de passe oublié ».");
        }
        Instant maintenant = greffe.maintenant();
        compte.definirMotDePasse(hachage.hacher(motDePasse), maintenant);
        compte.reussir(maintenant);
        comptes.mettreAJour(compte);
        greffe.noter(compte.identifiant(), utilisateur, EvenementConnexion.MOT_DE_PASSE_DEFINI, null, contexte);
        greffe.noter(compte.identifiant(), utilisateur, EvenementConnexion.CONNEXION_REUSSIE, "premier mot de passe",
                contexte);
        return utilisateur;
    }

    /** RG-AUT-06, 07, 12 : mot de passe actuel exigé ; les navigateurs de confiance sont oubliés. */
    public void changer(long utilisateur, String actuel, String nouveau, Contexte contexte) {
        CompteConnexion compte = comptes.parUtilisateur(utilisateur).orElseThrow(() -> new ElementIntrouvable(
                "Aucun compte pour cet utilisateur."));
        if (compte.motDePasse().filter(m -> hachage.correspond(actuel == null ? "" : actuel, m)).isEmpty()) {
            throw new RegleEnfreinte("RG-AUT-07", "Le mot de passe actuel est incorrect.");
        }
        CompteConnexion.verifierMotDePasse(nouveau, politique);
        compte.definirMotDePasse(hachage.hacher(nouveau), greffe.maintenant());
        comptes.mettreAJour(compte);
        greffe.noter(compte.identifiant(), utilisateur, EvenementConnexion.MOT_DE_PASSE_CHANGE, null, contexte);
    }

    /**
     * RG-AUT-08, 12 : un e-mail inconnu est signalé ; sinon un lien valable une heure part, et remplace la
     * demande précédente.
     */
    public void demanderReinitialisation(String email, Contexte contexte) {
        CompteConnexion compte = (email == null || email.isBlank() ? Optional.<CompteConnexion>empty()
                : comptes.parIdentifiant(CompteConnexion.identifiant(email)))
                .orElseThrow(() -> new RegleEnfreinte("RG-AUT-08", "Aucun compte pour cet e-mail."));
        String jeton = compte.demanderReinitialisation(greffe.maintenant(), politique);
        comptes.mettreAJour(compte);
        greffe.noter(email, compte.utilisateur(), EvenementConnexion.REINITIALISATION_DEMANDEE, null, contexte);
        greffe.reinitialisation(compte.identifiant(), jeton);
    }

    /**
     * RG-AUT-06 à 08, 12 : avec le lien reçu, un nouveau mot de passe ; le blocage temporaire est levé, les
     * navigateurs de confiance oubliés, les sessions du compte fermées.
     */
    public void reinitialiser(String jeton, String motDePasse, Contexte contexte) {
        CompteConnexion.verifierMotDePasse(motDePasse, politique);
        CompteConnexion compte = comptes.parEmpreinteReinitialisation(Secrets.empreinte(jeton == null ? "" : jeton))
                .orElseThrow(() -> new RegleEnfreinte("RG-AUT-08", "Ce lien de réinitialisation n'est pas valable :"
                        + " il a peut-être été remplacé par une nouvelle demande."));
        compte.reinitialiser(jeton, hachage.hacher(motDePasse), greffe.maintenant());
        comptes.mettreAJour(compte);
        sessions.fermer(compte.utilisateur());
        greffe.noter(compte.identifiant(), compte.utilisateur(), EvenementConnexion.MOT_DE_PASSE_REINITIALISE, null,
                contexte);
    }
}
