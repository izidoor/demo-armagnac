package fr.bnia.declarations.application;

import fr.bnia.commun.application.StockageDocuments;
import fr.bnia.commun.domain.DocumentDepose;
import fr.bnia.commun.domain.ElementIntrouvable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** Stockage de documents en mémoire ; les garde-fous (taille, signature PDF) sont testés sur l'adapter. */
final class StockageEnMemoire implements StockageDocuments {

    private final AtomicLong sequence = new AtomicLong(900);
    final Map<Long, DocumentLu> documents = new LinkedHashMap<>();

    @Override
    public DocumentDepose deposerPdf(String nom, byte[] contenu) {
        DocumentDepose document = new DocumentDepose(sequence.getAndIncrement(), nom, "application/pdf",
                contenu.length);
        documents.put(document.cle(), new DocumentLu(document, contenu));
        return document;
    }

    @Override
    public DocumentLu lire(long cle) {
        return Optional.ofNullable(documents.get(cle))
                .orElseThrow(() -> new ElementIntrouvable("Document introuvable : " + cle + "."));
    }

    @Override
    public void supprimer(long cle) {
        documents.remove(cle);
    }

    @Override
    public void supprimer(Collection<Long> cles) {
        cles.forEach(documents::remove);
    }
}
