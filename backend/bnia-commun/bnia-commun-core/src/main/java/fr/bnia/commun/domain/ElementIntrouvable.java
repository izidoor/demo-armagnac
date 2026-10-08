package fr.bnia.commun.domain;

/**
 * L'élément demandé n'existe pas.
 */
public class ElementIntrouvable extends RuntimeException {

    public ElementIntrouvable(String message) {
        super(message);
    }
}
