package fr.bnia.operateurs.domain.partage;

/**
 * Un utilisateur vient d'être créé, à l'acceptation d'une invitation (RG-OPE-42) : {@code authentification}
 * crée son compte de connexion, sans mot de passe, dans la même transaction (RG-AUT-11, ADR-015).
 *
 * @param utilisateur identifiant de l'utilisateur
 * @param email       son e-mail, identifiant de connexion (RG-AUT-10)
 */
public record UtilisateurCree(long utilisateur, String email) {
}
