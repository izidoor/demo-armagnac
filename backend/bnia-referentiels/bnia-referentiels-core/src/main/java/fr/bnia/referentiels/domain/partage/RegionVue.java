package fr.bnia.referentiels.domain.partage;

/**
 * Région vue par les autres contextes : {@code operateurs} en tire la distinction « région Armagnac /
 * hors région » (C.1.1.2).
 */
public record RegionVue(long id, String code, String libelle, boolean enRegionDelimitee, boolean actif) {
}
