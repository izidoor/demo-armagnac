package fr.bnia.declarations.domain;

/** Origine d'une ligne de DSM (H-DEC-11, RG-DEC-13). */
public enum OrigineLigne {

    /** Venue du stock initial ou de la DSM précédente : ne se supprime pas, son identité ne change pas. */
    REPRISE,
    /** Ajoutée par l'opérateur, colonne 1 à 0 : se supprime tant que la DSM est modifiable. */
    AJOUT
}
