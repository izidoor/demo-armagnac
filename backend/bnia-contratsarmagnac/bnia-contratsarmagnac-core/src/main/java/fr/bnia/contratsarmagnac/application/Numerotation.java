package fr.bnia.contratsarmagnac.application;

/** RG-CA-06 : le rang suivant dans l'année, jamais réattribué. */
public interface Numerotation {

    int suivant(int annee);
}
