package fr.bnia.declarations.domain;

/**
 * Clé de conditionnement d'une ligne de DSM (glossaire, §4.4.3 p.23, C.3.2.1 p.108). La première lettre
 * décide de ce que porte la ligne : {@code M}, un millésime ; {@code N}, une dénomination (RG-DEC-10).
 */
public enum CleConditionnement {

    MB("Millésime sous bois"),
    MV("Millésime sous verre"),
    NB("Assemblage sous bois"),
    NV("Assemblage sous verre");

    private final String libelle;

    CleConditionnement(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }

    /** RG-DEC-10 : {@code MB} et {@code MV} portent un millésime. */
    public boolean millesimee() {
        return this == MB || this == MV;
    }

    /** RG-DEC-12 : seul le compte d'une ligne {@code MB} est calculé. */
    public boolean compteCalcule() {
        return this == MB;
    }
}
