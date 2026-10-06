package fr.bnia.referentiels.application;

/** Ligne du listing des régions (modèle de lecture, ADR-004). */
public record RegionLigne(String code, String libelle, boolean enRegionDelimitee, boolean actif) {
}
