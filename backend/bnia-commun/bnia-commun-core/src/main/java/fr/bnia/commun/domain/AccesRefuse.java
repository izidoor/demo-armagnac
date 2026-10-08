package fr.bnia.commun.domain;

/**
 * L'acteur n'a pas le droit de faire cette action sur cet objet (RG-OPE-31 à 33, ADR-016) : levée par le
 * cas d'usage, qui vérifie les droits.
 */
public class AccesRefuse extends RegleEnfreinte {

    public AccesRefuse(String regle, String message) {
        super(regle, message);
    }
}
