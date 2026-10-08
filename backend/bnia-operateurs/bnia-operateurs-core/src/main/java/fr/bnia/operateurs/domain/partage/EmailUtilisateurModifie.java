package fr.bnia.operateurs.domain.partage;

/**
 * Un utilisateur a confirmé son nouvel e-mail (RG-OPE-48) : {@code authentification} change l'identifiant
 * de connexion dans la même transaction (RG-AUT-10, ADR-015).
 */
public record EmailUtilisateurModifie(long utilisateur, String email) {
}
