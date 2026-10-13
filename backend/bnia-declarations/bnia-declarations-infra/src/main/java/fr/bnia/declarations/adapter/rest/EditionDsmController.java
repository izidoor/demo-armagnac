package fr.bnia.declarations.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.declarations.application.EditerDsm;
import fr.bnia.declarations.application.EditerDsm.DocumentEdite;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Éditions d'une DSM (spec 05 §5.1, H-DEC-21) : le PDF, ouvert dans le navigateur. */
@RestController
@Tag(name = "declarations")
class EditionDsmController {

    private final EditerDsm editer;

    EditionDsmController(EditerDsm editer) {
        this.editer = editer;
    }

    @GetMapping(path = "/api/operateurs/{operateur}/dsm/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(operationId = "editerDsmPdf",
            summary = "Le PDF de la DSM, dans tous les états, « provisoire » avant validation ; inscrit au journal (RG-DEC-35)")
    ResponseEntity<byte[]> pdf(Connecte connecte, @PathVariable long operateur, @PathVariable long id) {
        DocumentEdite pdf = editer.pdf(Acteurs.de(connecte), operateur, id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(pdf.nom(), StandardCharsets.UTF_8).build().toString())
                .body(pdf.contenu());
    }
}
