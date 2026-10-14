package fr.bnia.declarations.domain;

/** Rôle d'une colonne dans le calcul du stock de fin (RG-DEC-17). */
public enum SensColonne {

    /** Colonne 1 : reprise, jamais saisie. */
    STOCK_DEBUT,
    ENTREE,
    SORTIE,
    /** Colonne 26 : calculée, jamais stockée. */
    STOCK_FIN
}
