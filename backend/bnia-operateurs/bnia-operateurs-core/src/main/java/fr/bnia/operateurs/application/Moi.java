package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.Utilisateur;
import java.util.List;
import java.util.Optional;

/**
 * L'utilisateur connecté, pour « Mon compte » et l'ouverture de l'application (vision UC-21, 22) : son
 * identité, ses rattachements, et l'opérateur où l'application s'ouvre directement (RG-OPE-49).
 *
 * @param operateurOuvert son opérateur actif, s'il y est utilisable ; vide : il choisit
 */
public record Moi(Utilisateur utilisateur, List<UtilisateurLigne.RattachementLigne> rattachements,
        Optional<Long> operateurOuvert) {
}
