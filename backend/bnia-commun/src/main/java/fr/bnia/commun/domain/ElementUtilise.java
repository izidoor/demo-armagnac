package fr.bnia.commun.domain;

/**
 * L'élément ne peut pas être supprimé : d'autres données y font référence. On le désactive à la place.
 */
public class ElementUtilise extends RegleEnfreinte {

    public ElementUtilise(String regle, String message) {
        super(regle, message);
    }
}
