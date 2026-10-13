package fr.bnia.declarations.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.commun.application.StockageDocuments.DocumentLu;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.declarations.adapter.rest.PiecesResponse.JustificatifResponse;
import fr.bnia.declarations.application.FichierDepose;
import fr.bnia.declarations.application.GererPieces;
import fr.bnia.declarations.domain.TypeMouvement;
import fr.bnia.declarations.domain.TypePiece;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Pièces d'une DSM (spec 05 §6.3, onglet « Pièces ») : justificatifs et DRM, déposés en PDF (multipart),
 * téléchargés en ligne. Mêmes endpoints des deux côtés (RG-DEC-01, 28).
 */
@RestController
@RequestMapping("/api/operateurs/{operateur}/dsm/{dsm}")
@Tag(name = "declarations")
class PieceController {

    private final GererPieces gerer;

    PieceController(GererPieces gerer) {
        this.gerer = gerer;
    }

    @GetMapping("/pieces")
    @Operation(operationId = "consulterPiecesDsm",
            summary = "Justificatifs, mois de DRM et manques de la DSM (RG-DEC-01, 26, 27, 29)")
    PiecesResponse pieces(Connecte connecte, @PathVariable long operateur, @PathVariable long dsm) {
        return PiecesResponse.depuis(gerer.pieces(Acteurs.de(connecte), operateur, dsm));
    }

    @PostMapping(path = "/justificatifs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "deposerJustificatifDsm",
            summary = "Dépose un justificatif PDF, sur la DSM ou une de ses lignes (RG-DEC-26, 28)")
    JustificatifResponse deposerJustificatif(Connecte connecte, @PathVariable long operateur, @PathVariable long dsm,
            @RequestPart("fichier") MultipartFile fichier, @RequestParam TypeMouvement typeMouvement,
            @RequestParam TypePiece typePiece, @RequestParam(required = false) Long ligne,
            @RequestParam(required = false) String reference) {
        return PiecesResponse.justificatif(gerer.deposerJustificatif(Acteurs.de(connecte), operateur, dsm, ligne,
                typeMouvement, typePiece, reference, fichier(fichier)));
    }

    @DeleteMapping("/justificatifs/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "supprimerJustificatifDsm", summary = "Supprime un justificatif et son fichier (RG-DEC-28)")
    void supprimerJustificatif(Connecte connecte, @PathVariable long operateur, @PathVariable long dsm,
            @PathVariable long id) {
        gerer.supprimerJustificatif(Acteurs.de(connecte), operateur, dsm, id);
    }

    @GetMapping(path = "/justificatifs/{id}/document", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(operationId = "telechargerJustificatifDsm", summary = "Le PDF d'un justificatif, à ouvrir dans le navigateur")
    ResponseEntity<byte[]> telechargerJustificatif(Connecte connecte, @PathVariable long operateur,
            @PathVariable long dsm, @PathVariable long id) {
        return pdf(gerer.telechargerJustificatif(Acteurs.de(connecte), operateur, dsm, id));
    }

    @PutMapping(path = "/drm/{mois}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deposerDrm",
            summary = "Dépose la DRM PDF d'un mois de la période ; refusé si le mois en a déjà une (RG-DEC-27, 28)")
    void deposerDrm(Connecte connecte, @PathVariable long operateur, @PathVariable long dsm,
            @PathVariable String mois, @RequestPart("fichier") MultipartFile fichier) {
        gerer.deposerDrm(Acteurs.de(connecte), operateur, dsm, mois(mois), fichier(fichier));
    }

    @DeleteMapping("/drm/{mois}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "supprimerDrm", summary = "Supprime la DRM d'un mois et son fichier (RG-DEC-28)")
    void supprimerDrm(Connecte connecte, @PathVariable long operateur, @PathVariable long dsm,
            @PathVariable String mois) {
        gerer.supprimerDrm(Acteurs.de(connecte), operateur, dsm, mois(mois));
    }

    @GetMapping(path = "/drm/{mois}/document", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(operationId = "telechargerDrm", summary = "Le PDF de la DRM d'un mois, à ouvrir dans le navigateur")
    ResponseEntity<byte[]> telechargerDrm(Connecte connecte, @PathVariable long operateur, @PathVariable long dsm,
            @PathVariable String mois) {
        return pdf(gerer.telechargerDrm(Acteurs.de(connecte), operateur, dsm, mois(mois)));
    }

    private static FichierDepose fichier(MultipartFile fichier) {
        try {
            return new FichierDepose(fichier.getOriginalFilename(), fichier.getBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static YearMonth mois(String mois) {
        try {
            return YearMonth.parse(mois);
        } catch (DateTimeParseException e) {
            throw new RegleEnfreinte(null, "Mois invalide : « " + mois + " » (attendu : AAAA-MM).");
        }
    }

    /** Ouvert dans le navigateur, sous son nom d'origine. */
    private static ResponseEntity<byte[]> pdf(DocumentLu lu) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(lu.document().nom(), StandardCharsets.UTF_8).build().toString())
                .body(lu.contenu());
    }
}
