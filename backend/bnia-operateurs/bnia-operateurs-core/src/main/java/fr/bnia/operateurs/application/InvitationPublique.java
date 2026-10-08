package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.partage.Role;
import java.time.Instant;

/**
 * Ce que voit la personne qui ouvre le lien d'une invitation (vision UC-20, spec 02 §6.3) : l'opérateur,
 * le rôle, et de quoi préremplir son identité ; si un compte existe pour l'e-mail, elle se connecte.
 */
public record InvitationPublique(
        String operateur,
        Role role,
        String email,
        Long titre,
        String nom,
        String prenom,
        boolean compteExistant,
        Instant expireLe) {
}
