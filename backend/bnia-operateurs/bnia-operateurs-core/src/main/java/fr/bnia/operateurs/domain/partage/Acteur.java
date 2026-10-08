package fr.bnia.operateurs.domain.partage;

/**
 * L'utilisateur connecté qui demande une action, tel que le controller le passe au cas d'usage (ADR-016,
 * A-OPE-7). Il ne porte que son identifiant : ses droits sont évalués à chaque requête (RG-OPE-31).
 *
 * @param utilisateur identifiant de l'utilisateur dans {@code operateurs}
 */
public record Acteur(long utilisateur) {
}
