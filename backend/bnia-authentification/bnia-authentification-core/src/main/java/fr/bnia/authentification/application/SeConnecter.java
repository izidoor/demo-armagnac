package fr.bnia.authentification.application;

import fr.bnia.authentification.domain.CompteConnexion;
import fr.bnia.authentification.domain.EvenementConnexion;
import fr.bnia.authentification.domain.Politique;
import fr.bnia.commun.application.UseCase;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.EtatConnexion;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Connexion unique de tous les utilisateurs (RG-AUT-01) : e-mail et mot de passe, puis un code envoyé par
 * e-mail si la double authentification est activée (RG-AUT-16), sauf sur un navigateur de confiance. {@code operateurs} dit si l'utilisateur peut se connecter
 * (RG-AUT-04). Les refus sont rendus, pas levés, pour que l'échec reste enregistré (RG-AUT-03).
 */
@UseCase
public class SeConnecter {

    private final CompteRepository comptes;
    private final HachageMotDePasse hachage;
    private final OperateursLecture operateurs;
    private final ReglageConnexion reglage;
    private final Politique politique;
    private final Greffe greffe;

    public SeConnecter(CompteRepository comptes, HachageMotDePasse hachage, OperateursLecture operateurs,
            ReglageConnexion reglage, JournalConnexions journal, EnvoiCourriels envoi,
            ParametresAuthentification parametres, Clock horloge) {
        this.comptes = comptes;
        this.hachage = hachage;
        this.operateurs = operateurs;
        this.reglage = reglage;
        this.politique = parametres.politique();
        this.greffe = new Greffe(journal, envoi, parametres, horloge);
    }

    /**
     * RG-AUT-01 à 04, 06, 12, 16 : vérifie l'e-mail et le mot de passe, puis l'accès ; envoie le code, ou
     * connecte directement, double authentification désactivée ou depuis un navigateur de confiance.
     *
     * @param jetonConfiance jeton du cookie de confiance de ce navigateur, ou {@code null}
     */
    public Resultat verifierMotDePasse(String email, String motDePasse, String jetonConfiance, Contexte contexte) {
        Instant maintenant = greffe.maintenant();
        Optional<CompteConnexion> trouve = email == null || email.isBlank() ? Optional.empty()
                : comptes.parIdentifiant(CompteConnexion.identifiant(email));
        if (trouve.isEmpty()) {
            greffe.noter(email, null, EvenementConnexion.ECHEC_MOT_DE_PASSE, "COMPTE_INCONNU", contexte);
            return new Resultat.Refuse("RG-AUT-02", "Aucun compte pour cet e-mail.");
        }
        CompteConnexion compte = trouve.get();
        if (compte.estBloque(maintenant)) {
            return refuserBloque(email, compte, contexte);
        }
        if (compte.motDePasse().filter(m -> hachage.correspond(motDePasse == null ? "" : motDePasse, m)).isEmpty()) {
            return echec(email, compte, EvenementConnexion.ECHEC_MOT_DE_PASSE, "Mot de passe incorrect.", contexte);
        }
        Optional<Resultat> refus = verifierAcces(email, compte, contexte);
        if (refus.isPresent()) {
            return refus.get();
        }
        if (!reglage.doubleAuthentification()) {
            return connecterSansCode(email, compte, "double authentification désactivée", contexte);
        }
        if (compte.estDeConfiance(jetonConfiance, maintenant)) {
            return connecterSansCode(email, compte, "navigateur de confiance", contexte);
        }
        String code = compte.emettreCode(maintenant, politique);
        comptes.mettreAJour(compte);
        greffe.code(compte.identifiant(), code);
        return new Resultat.CodeEnvoye(compte.utilisateur());
    }

    /**
     * RG-AUT-03 à 06, 12 : le code envoyé, une fois ; un code faux compte comme un échec (H-AUT-2). Le
     * navigateur peut être mis en confiance pour 30 jours.
     */
    public Resultat verifierCode(long utilisateur, String code, boolean faireConfiance, Contexte contexte) {
        Instant maintenant = greffe.maintenant();
        Optional<CompteConnexion> trouve = comptes.parUtilisateur(utilisateur);
        if (trouve.isEmpty()) {
            return new Resultat.Refuse("RG-AUT-04", "Aucun accès actif : reconnectez-vous.");
        }
        CompteConnexion compte = trouve.get();
        String email = compte.identifiant();
        if (compte.estBloque(maintenant)) {
            return refuserBloque(email, compte, contexte);
        }
        Optional<Resultat> refus = verifierAcces(email, compte, contexte);
        if (refus.isPresent()) {
            return refus.get();
        }
        if (!compte.verifierCode(code, maintenant)) {
            return echec(email, compte, EvenementConnexion.ECHEC_CODE, "Code incorrect ou expiré : saisissez le"
                    + " dernier code reçu, ou demandez-en un nouveau.", contexte);
        }
        compte.reussir(maintenant);
        Optional<String> jeton = Optional.empty();
        if (faireConfiance) {
            jeton = Optional.of(compte.faireConfiance(maintenant, politique));
            greffe.noter(email, utilisateur, EvenementConnexion.NAVIGATEUR_DE_CONFIANCE, null, contexte);
        }
        comptes.mettreAJour(compte);
        greffe.noter(email, utilisateur, EvenementConnexion.CONNEXION_REUSSIE, null, contexte);
        return new Resultat.Connecte(utilisateur, jeton);
    }

    /** RG-AUT-05 : un nouveau code invalide le précédent. */
    public Resultat renvoyerCode(long utilisateur, Contexte contexte) {
        Instant maintenant = greffe.maintenant();
        Optional<CompteConnexion> trouve = comptes.parUtilisateur(utilisateur);
        if (trouve.isEmpty()) {
            return new Resultat.Refuse("RG-AUT-04", "Aucun accès actif : reconnectez-vous.");
        }
        CompteConnexion compte = trouve.get();
        if (compte.estBloque(maintenant)) {
            return refuserBloque(compte.identifiant(), compte, contexte);
        }
        String code = compte.emettreCode(maintenant, politique);
        comptes.mettreAJour(compte);
        greffe.code(compte.identifiant(), code);
        return new Resultat.CodeEnvoye(utilisateur);
    }

    /** RG-AUT-09, 12. */
    public void seDeconnecter(long utilisateur, Contexte contexte) {
        String email = comptes.parUtilisateur(utilisateur).map(CompteConnexion::identifiant).orElse("");
        greffe.noter(email, utilisateur, EvenementConnexion.DECONNEXION, null, contexte);
    }

    /** RG-AUT-06, 16 : connecté sans code ; le journal dit pourquoi. */
    private Resultat connecterSansCode(String email, CompteConnexion compte, String motif, Contexte contexte) {
        compte.reussir(greffe.maintenant());
        comptes.mettreAJour(compte);
        greffe.noter(email, compte.utilisateur(), EvenementConnexion.CONNEXION_REUSSIE, motif, contexte);
        return new Resultat.Connecte(compte.utilisateur(), Optional.empty());
    }

    /** RG-AUT-04 : non bloqué par le BNIA, au moins un rattachement actif dans un opérateur actif. */
    private Optional<Resultat> verifierAcces(String email, CompteConnexion compte, Contexte contexte) {
        EtatConnexion etat = operateurs.etatConnexion(compte.utilisateur());
        if (etat == EtatConnexion.AUTORISE) {
            return Optional.empty();
        }
        greffe.noter(email, compte.utilisateur(), EvenementConnexion.CONNEXION_REFUSEE,
                etat == EtatConnexion.UTILISATEUR_BLOQUE ? "UTILISATEUR_BLOQUE" : "AUCUN_ACCES_ACTIF", contexte);
        return Optional.of(new Resultat.Refuse("RG-AUT-04", etat == EtatConnexion.UTILISATEUR_BLOQUE
                ? "Compte bloqué, contactez le BNIA."
                : "Aucun accès actif : votre accès a été suspendu ou retiré dans tous vos opérateurs."));
    }

    /** RG-AUT-03 : toute tentative est refusée pendant le blocage, même juste. */
    private Resultat refuserBloque(String email, CompteConnexion compte, Contexte contexte) {
        greffe.noter(email, compte.utilisateur(), EvenementConnexion.CONNEXION_REFUSEE, "BLOQUE_TEMPORAIREMENT", contexte);
        return bloque(compte);
    }

    private static Resultat bloque(CompteConnexion compte) {
        return new Resultat.Refuse("RG-AUT-03", "Compte bloqué jusqu'à " + Greffe.HEURE.format(compte.bloqueJusqua()
                .orElseThrow()) + " après trop d'échecs. Vous pouvez aussi utiliser « mot de passe oublié ».");
    }

    /** RG-AUT-02, 03 : l'échec est compté ; au troisième, le compte est bloqué. */
    private Resultat echec(String email, CompteConnexion compte, EvenementConnexion evenement, String message,
            Contexte contexte) {
        Instant maintenant = greffe.maintenant();
        boolean bloque = compte.enregistrerEchec(maintenant, politique);
        comptes.mettreAJour(compte);
        greffe.noter(email, compte.utilisateur(), evenement, null, contexte);
        if (bloque) {
            greffe.noter(email, compte.utilisateur(), EvenementConnexion.BLOCAGE_DECLENCHE, null, contexte);
            return bloque(compte);
        }
        return new Resultat.Refuse(evenement == EvenementConnexion.ECHEC_CODE ? "RG-AUT-05" : "RG-AUT-02", message);
    }
}
