package fr.bnia.referentiels.application;

/**
 * Ligne d'un listing de liste simple (modèle de lecture, ADR-004).
 */
public record ValeurSimpleLigne(long id, String code, String libelle, boolean actif) {
}
