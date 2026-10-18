package fr.bnia.messagerie.api;

/**
 * L'utilisateur qui agit dans {@code messagerie} : son seul identifiant, ses droits étant lus à chaque appel par le
 * port de l'hôte (RG-MSG-01, 02). Le contexte appelant traduit le sien en celui-ci.
 *
 * @param utilisateur identifiant de l'utilisateur chez l'hôte
 */
public record Acteur(long utilisateur) {
}
