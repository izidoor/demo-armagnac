package fr.bnia.operateurs.domain.partage;

/**
 * Type d'opérateur (C.1.1.1 p.100, §4.3 p.18) : liste fermée, qui pilote la forme du code, la
 * périodicité DSM par défaut, les colonnes de DSM et l'assiette de cotisation (RG-OPE-01). Une fiche
 * {@link #AUTRE} (distillateur, courtier…) ne déclare pas. {@link #BNIA} désigne l'interprofession
 * elle-même, opérateur unique créé par l'application (RG-OPE-39).
 */
public enum TypeOperateur {

    /** L'interprofession : unique, jamais créée ni choisie à l'écran (RG-OPE-39). */
    BNIA("BNIA"),
    PRODUCTEUR("Producteur"),
    NEGOCIANT("Négociant"),
    AUTRE("Autre");

    private final String libelle;

    TypeOperateur(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }

    /** RG-OPE-01 : {@code AUTRE} et {@code BNIA} ne déclarent pas. */
    public boolean declare() {
        return this == PRODUCTEUR || this == NEGOCIANT;
    }
}
