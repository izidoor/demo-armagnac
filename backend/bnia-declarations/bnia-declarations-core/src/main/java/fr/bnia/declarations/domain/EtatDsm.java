package fr.bnia.declarations.domain;

/**
 * États d'instruction d'une DSM (analyse §2 et §3, spec 05 §3.2). {@code BROUILLON} à l'initiation ; les
 * transitions (RG-DEC-20) sont portées par {@link DeclarationStocksMouvements}. Chaque côté lit l'état de son
 * point de vue (revue du 2026-10-13) : une DSM « envoyée » par l'opérateur est « à prendre en charge » par le
 * BNIA ; des compléments « envoyés » sont « reçus ».
 */
public enum EtatDsm {

    BROUILLON("Brouillon", "Brouillon"),
    ENVOYEE("Envoyée", "À prendre en charge"),
    EN_INSTRUCTION("En instruction", "En instruction"),
    COMPLEMENTS_DEMANDES("Compléments demandés", "Compléments demandés"),
    COMPLEMENTS_RECUS("Compléments envoyés", "Compléments reçus"),
    VALIDEE("Validée", "Validée");

    private final String libelleOperateur;
    private final String libelleBnia;

    EtatDsm(String libelleOperateur, String libelleBnia) {
        this.libelleOperateur = libelleOperateur;
        this.libelleBnia = libelleBnia;
    }

    /** Le libellé de l'opérateur, celui des messages des règles. */
    public String libelle() {
        return libelleOperateur;
    }

    /** Le libellé lu de ce côté. */
    public String libelle(Cote cote) {
        return cote == Cote.BNIA ? libelleBnia : libelleOperateur;
    }

    /** RG-DEC-21 : la DSM est chez l'opérateur, qui la modifie. */
    public boolean modifiable() {
        return this == BROUILLON || this == COMPLEMENTS_DEMANDES;
    }
}
