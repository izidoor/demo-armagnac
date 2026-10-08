package fr.bnia.operateurs.domain.partage;

/**
 * Statut de déclaration de stock (C.1.1.2 p.102). Seul {@link #OUI} rend une DSM attendue (RG-OPE-11).
 * Sens des valeurs : H-OPE-2, à confirmer par le BNIA (Q-OPE-7).
 */
public enum StatutDeclarationStock {

    /** Déclare. */
    OUI("Oui"),
    /** N'est pas concerné. */
    NON("Non"),
    /** Suspendu, reprise possible. */
    SOMMEIL("Sommeil"),
    /** A cessé. */
    CLOTURE("Clôturé");

    private final String libelle;

    StatutDeclarationStock(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }

    /** RG-OPE-11 : {@code OUI} pour un producteur ou un négociant, {@code NON} pour {@code AUTRE}. */
    public static StatutDeclarationStock parDefaut(TypeOperateur type) {
        return type == TypeOperateur.AUTRE ? NON : OUI;
    }
}
