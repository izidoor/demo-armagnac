package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.partage.Role;

/**
 * Invitation d'une personne (vision UC-02, 12) : depuis un contact de l'opérateur, ou une personne
 * nouvelle (titre, nom, prénom, fonction, téléphone professionnel) ; e-mail et rôle obligatoires
 * (RG-OPE-40).
 *
 * @param contact identifiant du contact invité, ou {@code null} pour une personne nouvelle
 */
public record SaisieInvitation(
        Long contact,
        String email,
        Role role,
        Long titre,
        String nom,
        String prenom,
        String fonction,
        String telephoneProfessionnel) {

    /** Une personne nouvelle. */
    public static SaisieInvitation personne(String email, Role role, String nom, String prenom) {
        return new SaisieInvitation(null, email, role, null, nom, prenom, null, null);
    }
}
