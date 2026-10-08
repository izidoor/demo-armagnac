package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.StatutRattachement;
import fr.bnia.operateurs.domain.partage.Role;

/**
 * Une ligne de l'annuaire d'un opérateur (RG-OPE-52, vision §2.2) : un contact, un membre (nom et e-mail
 * lus dans l'utilisateur), ou la personne nouvelle d'une invitation en attente.
 *
 * @param id                  identifiant du contact, du rattachement ou de l'invitation, selon le genre
 * @param fiche               fiche professionnelle ; vide pour une invitation
 * @param role                rôle du membre, ou rôle proposé par l'invitation
 * @param invitationEnAttente identifiant de l'invitation en attente (personne nouvelle, ou contact invité)
 */
public record PersonneAnnuaire(
        Genre genre,
        long id,
        Long fiche,
        Long titre,
        String nom,
        String prenom,
        String email,
        String fonction,
        String telephoneProfessionnel,
        Role role,
        StatutRattachement statut,
        boolean bloqueParBnia,
        Long invitationEnAttente) {

    /** CONTACT, MEMBRE, INVITATION. */
    public enum Genre {
        CONTACT, MEMBRE, INVITATION
    }
}
