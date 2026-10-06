package fr.bnia.operateurs.application;

/**
 * Identifiant d'un nouveau contact, attribué avant l'enregistrement : il reste stable quand l'agrégat
 * est réécrit, et sert aussitôt au journal et aux événements (décision du 2026-10-06).
 */
public interface IdentifiantsContacts {

    long prochain();
}
