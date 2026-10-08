package fr.bnia.boot.demo;

import fr.bnia.authentification.application.Contexte;
import fr.bnia.authentification.application.GererMotDePasse;
import fr.bnia.operateurs.application.GererInvitations;
import fr.bnia.operateurs.application.RattachementRepository;
import fr.bnia.operateurs.application.SaisieAcceptation;
import fr.bnia.operateurs.application.SaisieInvitation;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.Invitation;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.Role;
import java.util.Optional;

/**
 * Personnes et accès de démonstration (spec 02 §7.1, spec 03 §7.1), par les mêmes cas d'usage que l'écran :
 * une invitation, son acceptation, puis le premier mot de passe. Relançable : une personne déjà membre de
 * l'opérateur est laissée telle quelle.
 */
final class ComptesDemo {

    /** Mot de passe de tous les comptes de démonstration (docs/demo.md) ; le code vaut 000000 (RG-AUT-01). */
    static final String MOT_DE_PASSE = "demo-armagnac";

    private final GererInvitations invitations;
    private final GererMotDePasse motsDePasse;
    private final RattachementRepository rattachements;
    private final OperateursLecture lecture;

    ComptesDemo(GererInvitations invitations, GererMotDePasse motsDePasse, RattachementRepository rattachements,
            OperateursLecture lecture) {
        this.invitations = invitations;
        this.motsDePasse = motsDePasse;
        this.rattachements = rattachements;
        this.lecture = lecture;
    }

    /**
     * Un membre de l'opérateur, avec un compte de mot de passe {@link #MOT_DE_PASSE}.
     *
     * @param invitant {@code null} : l'application (premier ADMIN du BNIA)
     * @return l'identifiant de l'utilisateur
     */
    long membre(Acteur invitant, long operateur, Role role, String email, String nom, String prenom, String fonction) {
        Optional<Long> existant = lecture.utilisateurParEmail(email);
        if (existant.isPresent() && rattachements.de(operateur, existant.get()).isPresent()) {
            return existant.get();
        }
        var saisie = new SaisieInvitation(null, email, role, null, nom, prenom, fonction, null);
        Invitation.Emise emise = invitant == null ? invitations.emettre(null, operateur, saisie)
                : invitations.inviter(invitant, operateur, saisie);
        if (existant.isPresent()) {
            invitations.accepter(emise.jeton(), Optional.of(new Acteur(existant.get())), null);
            return existant.get();
        }
        long utilisateur = invitations.accepter(emise.jeton(), Optional.empty(),
                new SaisieAcceptation(null, nom, prenom)).utilisateur();
        motsDePasse.definirPremierMotDePasse(emise.jeton(), MOT_DE_PASSE, Contexte.inconnu());
        return utilisateur;
    }
}
