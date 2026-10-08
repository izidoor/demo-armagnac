package fr.bnia.operateurs.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.ActionJournal;
import fr.bnia.operateurs.domain.Changement;
import fr.bnia.operateurs.domain.Email;
import fr.bnia.operateurs.domain.IdentitePersonne;
import fr.bnia.operateurs.domain.Jeton;
import fr.bnia.operateurs.domain.StatutRattachement;
import fr.bnia.operateurs.domain.TypeCible;
import fr.bnia.operateurs.domain.Utilisateur;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.EmailUtilisateurModifie;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

/**
 * « Mon compte » (vision UC-21 à 24, spec 02 §6.3) : ce que tout utilisateur fait sans rôle particulier
 * (RG-OPE-33) : son profil, son e-mail, son opérateur actif. Le mot de passe est à
 * {@code authentification}.
 */
@UseCase
public class MonCompte {

    private final UtilisateurRepository utilisateurs;
    private final UtilisateursLecture lecture;
    private final PublicationEvenements publication;
    private final ParametresOperateurs parametres;
    private final References references;
    private final Journaliste journaliste;
    private final Courrier courrier;

    public MonCompte(UtilisateurRepository utilisateurs, UtilisateursLecture lecture, JournalActions journal,
            PublicationEvenements publication, EnvoiCourriels envoi, ParametresOperateurs parametres,
            ReferentielsLecture referentiels, Clock horloge) {
        this.utilisateurs = utilisateurs;
        this.lecture = lecture;
        this.publication = publication;
        this.parametres = parametres;
        this.references = new References(referentiels);
        this.journaliste = new Journaliste(journal, utilisateurs, referentiels, horloge);
        this.courrier = new Courrier(envoi, parametres);
    }

    /** RG-OPE-49 : l'opérateur actif n'ouvre l'application que s'il est actif et le rattachement {@code ACTIF}. */
    public Moi moi(Acteur acteur) {
        Utilisateur utilisateur = utilisateur(acteur);
        List<UtilisateurLigne.RattachementLigne> rattachements = lecture.utilisateur(utilisateur.id())
                .map(UtilisateurLigne::rattachements).orElse(List.of());
        Optional<Long> ouvert = utilisateur.operateurActif()
                .filter(op -> rattachements.stream().anyMatch(r -> r.operateur() == op && utilisable(r)));
        return new Moi(utilisateur, rattachements, ouvert);
    }

    /** RG-OPE-27, 34, 47, 51 : visible aussitôt dans tous ses opérateurs. */
    public Utilisateur modifierProfil(Acteur acteur, SaisieProfil saisie) {
        Utilisateur utilisateur = utilisateur(acteur);
        IdentitePersonne identite = IdentitePersonne.utilisateur(saisie.titre(), saisie.nom(), saisie.prenom());
        references.titre(identite.titre(), utilisateur.identite().titre());
        List<Changement> changements = utilisateur.modifierProfil(identite, saisie.version());
        utilisateurs.mettreAJour(utilisateur);
        if (!changements.isEmpty()) {
            journaliste.noter(acteur, null, ActionJournal.PROFIL_MODIFIE, TypeCible.UTILISATEUR, utilisateur.id(),
                    identite.nomComplet(), changements);
        }
        return utilisateur;
    }

    /**
     * RG-OPE-48 : refusé si l'e-mail est celui d'un autre utilisateur ; une seule demande en cours ; un lien
     * valable 24 heures part au nouvel e-mail.
     */
    public Utilisateur demanderChangementEmail(Acteur acteur, String email) {
        Utilisateur utilisateur = utilisateur(acteur);
        Email nouvel = Email.de(email, "RG-OPE-48");
        verifierLibre(nouvel, utilisateur.id());
        String jeton = utilisateur.demanderChangementEmail(nouvel, journaliste.maintenant(),
                parametres.validiteChangementEmail());
        utilisateurs.mettreAJour(utilisateur);
        courrier.changementEmail(nouvel.valeur(), jeton, utilisateur.changementEmail().orElseThrow().expireLe());
        return utilisateur;
    }

    /**
     * RG-OPE-34, 48 : en une transaction, l'e-mail et l'identifiant de connexion changent
     * ({@code EmailUtilisateurModifie}), la demande est close ; refusé si l'e-mail a été pris entre-temps.
     * Une notification part à l'ancien e-mail.
     */
    public Utilisateur confirmerChangementEmail(String jeton) {
        Utilisateur utilisateur = utilisateurs.parEmpreinteChangementEmail(Jeton.empreinte(jeton == null ? "" : jeton))
                .orElseThrow(() -> new ElementIntrouvable("Ce lien de confirmation n'est pas valable : il a peut-être"
                        + " été remplacé par une nouvelle demande."));
        String ancien = utilisateur.email().valeur();
        utilisateur.changementEmail().ifPresent(c -> verifierLibre(c.nouvelEmail(), utilisateur.id()));
        List<Changement> changements = utilisateur.confirmerChangementEmail(journaliste.maintenant());
        utilisateurs.mettreAJour(utilisateur);
        publication.publier(new EmailUtilisateurModifie(utilisateur.id(), utilisateur.email().valeur()));
        journaliste.noter(new Acteur(utilisateur.id()), null, ActionJournal.EMAIL_MODIFIE, TypeCible.UTILISATEUR,
                utilisateur.id(), utilisateur.identite().nomComplet(), changements);
        courrier.emailChange(ancien, utilisateur.email().valeur());
        return utilisateur;
    }

    /** RG-OPE-49 : parmi ses rattachements utilisables ; il en change à tout moment. */
    public Moi choisirOperateurActif(Acteur acteur, long operateur) {
        Utilisateur utilisateur = utilisateur(acteur);
        boolean utilisable = lecture.utilisateur(utilisateur.id()).map(UtilisateurLigne::rattachements)
                .orElse(List.of()).stream().anyMatch(r -> r.operateur() == operateur && utilisable(r));
        if (!utilisable) {
            throw new RegleEnfreinte("RG-OPE-49", "Vous n'avez pas d'accès actif à cet opérateur.");
        }
        utilisateur.choisirOperateurActif(operateur);
        utilisateurs.mettreAJour(utilisateur);
        return moi(acteur);
    }

    private void verifierLibre(Email email, long sauf) {
        utilisateurs.parEmail(email).filter(u -> u.id() != sauf).ifPresent(u -> {
            throw new ConflitUnicite("RG-OPE-48", "« " + email + " » est déjà l'e-mail d'un autre compte.");
        });
    }

    private static boolean utilisable(UtilisateurLigne.RattachementLigne r) {
        return r.statut() == StatutRattachement.ACTIF && r.operateurEnActivite();
    }

    private Utilisateur utilisateur(Acteur acteur) {
        return utilisateurs.parId(acteur.utilisateur())
                .orElseThrow(() -> new ElementIntrouvable("Utilisateur " + acteur.utilisateur() + " introuvable."));
    }
}
