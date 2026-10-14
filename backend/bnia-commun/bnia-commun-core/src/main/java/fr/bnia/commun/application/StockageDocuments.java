package fr.bnia.commun.application;

import fr.bnia.commun.domain.DocumentDepose;
import java.util.Collection;

/**
 * Stockage des fichiers déposés, commun à tous les contextes (ADR-018). Chaque opération s'exécute dans la
 * transaction de l'appelant : un document et sa référence sont écrits ou annulés ensemble. Le contexte qui
 * détient la référence vérifie les droits avant de lire.
 */
public interface StockageDocuments {

    /** Taille maximale d'un fichier déposé, en octets (10 Mo). */
    long TAILLE_MAXIMALE = 10L * 1024 * 1024;

    /**
     * Dépose un PDF, vérifié par sa signature {@code %PDF} et non par son extension.
     *
     * @throws fr.bnia.commun.domain.DocumentRefuse si le fichier est vide, trop gros ou n'est pas un PDF
     */
    DocumentDepose deposerPdf(String nom, byte[] contenu);

    /** @throws fr.bnia.commun.domain.ElementIntrouvable si le document n'existe pas */
    DocumentLu lire(long cle);

    void supprimer(long cle);

    /** Supprime ces documents en une fois (les pièces d'une déclaration supprimée, par exemple). */
    void supprimer(Collection<Long> cles);

    /** Un document et son contenu. */
    record DocumentLu(DocumentDepose document, byte[] contenu) {
    }
}
