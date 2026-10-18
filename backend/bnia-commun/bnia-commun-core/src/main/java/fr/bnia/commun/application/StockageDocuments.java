package fr.bnia.commun.application;

import fr.bnia.commun.domain.DocumentDepose;
import java.util.Collection;
import java.util.Set;

/**
 * Stockage des fichiers déposés, commun à tous les contextes (ADR-018). Chaque opération s'exécute dans la
 * transaction de l'appelant : un document et sa référence sont écrits ou annulés ensemble. Le contexte qui
 * détient la référence vérifie les droits avant de lire.
 */
public interface StockageDocuments {

    /** Taille maximale d'un fichier déposé, en octets (10 Mo). */
    long TAILLE_MAXIMALE = 10L * 1024 * 1024;

    /**
     * Dépose un fichier de l'un des formats admis, reconnu par sa signature et non par son extension (ADR-018 amendé
     * le 2026-10-17) ; son type MIME est celui du format reconnu.
     *
     * @throws fr.bnia.commun.domain.DocumentRefuse si le fichier est vide, trop gros ou d'aucun format admis
     */
    DocumentDepose deposer(String nom, byte[] contenu, Set<FormatDocument> admis);

    /**
     * Dépose un PDF, vérifié par sa signature {@code %PDF} et non par son extension.
     *
     * @throws fr.bnia.commun.domain.DocumentRefuse si le fichier est vide, trop gros ou n'est pas un PDF
     */
    default DocumentDepose deposerPdf(String nom, byte[] contenu) {
        return deposer(nom, contenu, Set.of(FormatDocument.PDF));
    }

    /** @throws fr.bnia.commun.domain.ElementIntrouvable si le document n'existe pas */
    DocumentLu lire(long cle);

    void supprimer(long cle);

    /** Supprime ces documents en une fois (les pièces d'une déclaration supprimée, par exemple). */
    void supprimer(Collection<Long> cles);

    /** Un document et son contenu. */
    record DocumentLu(DocumentDepose document, byte[] contenu) {
    }
}
