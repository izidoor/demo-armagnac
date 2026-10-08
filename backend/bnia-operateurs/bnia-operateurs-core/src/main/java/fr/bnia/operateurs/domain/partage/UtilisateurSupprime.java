package fr.bnia.operateurs.domain.partage;

/**
 * Un utilisateur a perdu son dernier rattachement (RG-OPE-25, 44) : {@code authentification} supprime son
 * compte de connexion dans la même transaction (RG-AUT-11, ADR-015).
 */
public record UtilisateurSupprime(long utilisateur) {
}
