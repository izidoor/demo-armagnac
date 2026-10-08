package fr.bnia.commun.adapter.rest;

/**
 * L'utilisateur connecté, paramètre d'un endpoint (ADR-015, ADR-016) : le controller le traduit en acteur
 * pour le cas d'usage, qui vérifie ses droits. Résolu depuis la session par {@link ConnecteResolver} ;
 * sans session, la requête est refusée (401). Un paramètre {@code Optional<Connecte>} accepte une requête
 * anonyme. Absent du contrat OpenAPI.
 *
 * @param utilisateur identifiant de l'utilisateur dans {@code operateurs}
 */
public record Connecte(long utilisateur) {
}
