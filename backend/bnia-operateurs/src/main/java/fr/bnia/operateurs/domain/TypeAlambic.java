package fr.bnia.operateurs.domain;

/**
 * Type d'alambic (C.1.2.1 p.102, H-OPE-3) : les deux types que le CDC cite ; types et unités de capacité
 * en usage demandés au BNIA (Q-OPE-8).
 */
public enum TypeAlambic {

    ARMAGNACAIS("Armagnacais (continu)"),
    DOUBLE_CHAUFFE("Double chauffe");

    private final String libelle;

    TypeAlambic(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
