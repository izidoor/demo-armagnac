package fr.bnia.operateurs.application;

/**
 * Résultat de l'acceptation d'une invitation (RG-OPE-42).
 *
 * @param nouveauCompte vrai si l'utilisateur vient d'être créé : il pose aussitôt son mot de passe (RG-AUT-15)
 */
public record Acceptation(long utilisateur, long operateur, long rattachement, boolean nouveauCompte) {
}
