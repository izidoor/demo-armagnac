package fr.bnia.messagerie.domain;

/**
 * Le côté d'un acteur (RG-MSG-03) : l'opérateur, ou le BNIA. Un message porte le côté de son auteur et s'adresse à
 * l'autre ; il est lu ou non lu par côté (RG-MSG-09).
 */
public enum Cote {

    // Même ordre que le côté de declarations : le contrat OpenAPI n'a qu'un schéma « Cote ».
    BNIA,
    OPERATEUR;

    public Cote autre() {
        return this == OPERATEUR ? BNIA : OPERATEUR;
    }
}
