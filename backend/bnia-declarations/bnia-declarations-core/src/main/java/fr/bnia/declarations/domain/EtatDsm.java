package fr.bnia.declarations.domain;

/**
 * États d'instruction d'une DSM (analyse §2 et §3, spec 05 §3.2). {@code BROUILLON} à l'initiation ; les
 * transitions (RG-DEC-20) viennent avec le lot 2.
 */
public enum EtatDsm {

    BROUILLON("Brouillon"),
    ENVOYEE("Envoyée"),
    EN_INSTRUCTION("En instruction"),
    COMPLEMENTS_DEMANDES("Compléments demandés"),
    VALIDEE("Validée");

    private final String libelle;

    EtatDsm(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }

    /** RG-DEC-21 : la DSM est chez l'opérateur, qui la modifie. */
    public boolean modifiable() {
        return this == BROUILLON || this == COMPLEMENTS_DEMANDES;
    }
}
