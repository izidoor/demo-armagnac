package fr.bnia.commun.domain;

/**
 * Un fichier déposé est refusé par le stockage (ADR-018) : vide, trop gros, ou d'un autre format que celui
 * attendu. Le message nomme le fichier. Traduite en 422, comme toute règle enfreinte.
 */
public class DocumentRefuse extends RegleEnfreinte {

    public DocumentRefuse(String message) {
        super(null, message);
    }
}
