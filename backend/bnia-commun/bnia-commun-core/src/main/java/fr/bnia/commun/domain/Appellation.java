package fr.bnia.commun.domain;

/**
 * Appellation d'origine contrôlée (glossaire, C.1.1.2 p.102, AGI 2.1.1 p.5) : liste fermée, fixée par
 * le cahier des charges de l'AOC, pas administrable. Blanche Armagnac comprise (décision du 2026-10-06,
 * spec 02). Concept partagé par plusieurs contextes (ADR-009, A-OPE-4).
 */
public enum Appellation {

    ARMAGNAC("Armagnac"),
    BAS_ARMAGNAC("Bas Armagnac"),
    ARMAGNAC_TENAREZE("Armagnac Ténarèze"),
    HAUT_ARMAGNAC("Haut Armagnac"),
    BLANCHE_ARMAGNAC("Blanche Armagnac");

    private final String libelle;

    Appellation(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
