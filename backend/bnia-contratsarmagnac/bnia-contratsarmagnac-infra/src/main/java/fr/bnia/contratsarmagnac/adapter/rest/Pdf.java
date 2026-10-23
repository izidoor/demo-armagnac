package fr.bnia.contratsarmagnac.adapter.rest;

import fr.bnia.contratsarmagnac.application.EditerContrats.DocumentEdite;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/** Un PDF ouvert dans le navigateur, sous son nom de fichier. */
final class Pdf {

    private Pdf() {
    }

    static ResponseEntity<byte[]> reponse(DocumentEdite document) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(document.nom(), StandardCharsets.UTF_8).build().toString())
                .body(document.contenu());
    }
}
