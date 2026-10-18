package fr.bnia.facturation.api;

/**
 * L'utilisateur qui demande une action à {@code facturation} : son seul identifiant, ses droits étant lus à
 * chaque appel par le port de l'hôte (RG-FAC-26). La source traduit le sien en celui-ci.
 *
 * @param utilisateur identifiant de l'utilisateur chez l'hôte
 */
public record Acteur(long utilisateur) {
}
