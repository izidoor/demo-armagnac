package fr.bnia.declarations.domain;

/**
 * Le côté d'un acteur : le BNIA qui instruit, ou l'opérateur qui déclare. Il porte l'auteur d'un commentaire
 * (spec 05 §3.2.4) et le libellé des états d'une DSM, lus différemment de chaque côté (§3.2).
 */
public enum Cote {

    BNIA,
    OPERATEUR
}
