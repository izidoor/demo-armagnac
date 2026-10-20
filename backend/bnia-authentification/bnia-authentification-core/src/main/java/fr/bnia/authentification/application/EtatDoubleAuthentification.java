package fr.bnia.authentification.application;

/**
 * La double authentification (RG-AUT-16), telle que l'écran du back-office la montre.
 *
 * @param modifiable l'acteur est ADMIN du BNIA
 */
public record EtatDoubleAuthentification(boolean active, boolean modifiable) {
}
