package fr.bnia.facturation.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.commun.application.StockageDocuments.DocumentLu;
import fr.bnia.facturation.adapter.rest.VentesResponse.LigneVendueResponse;
import fr.bnia.facturation.application.ConsulterMesFactures;
import fr.bnia.facturation.application.FiltrePieces;
import fr.bnia.facturation.domain.SensPiece;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Les factures et avoirs d'un opérateur, au portail (UC-FAC-5, spec 08 §6.2) : pièces émises dont il est le client,
 * détail et PDF, consultations journalisées côté client (RG-FAC-26, 28) ; lignes article d'une période (RG-FAC-34).
 */
@RestController
@RequestMapping("/api/operateurs/{operateur}/factures")
@Tag(name = "facturation")
class MesFacturesController {

    private final ConsulterMesFactures mesFactures;

    MesFacturesController(ConsulterMesFactures mesFactures) {
        this.mesFactures = mesFactures;
    }

    @GetMapping
    @Operation(operationId = "listerMesFactures",
            summary = "Les factures et avoirs émis dont l'opérateur est le client, filtrés (RG-FAC-26, 28)")
    List<PieceListeeResponse> lister(Connecte connecte, @PathVariable long operateur,
            @Parameter(description = "Début du numéro") @RequestParam(required = false) String numero,
            @RequestParam(required = false) SensPiece sens,
            @Parameter(description = "Nature d'origine", example = "DSM") @RequestParam(required = false) String nature,
            @Parameter(description = "Contenu dans le libellé de la période") @RequestParam(required = false) String periode,
            @Parameter(description = "Date d'émission au plus tôt")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
            @Parameter(description = "Date d'émission au plus tard")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) {
        FiltrePieces filtre = new FiltrePieces(texte(numero), Optional.ofNullable(sens), Optional.empty(),
                texte(nature), Optional.empty(), Optional.empty(), texte(periode), Optional.ofNullable(du),
                Optional.ofNullable(au));
        return mesFactures.lister(Acteurs.de(connecte), operateur, filtre).stream().map(PieceListeeResponse::de)
                .toList();
    }

    @GetMapping("/lignes")
    @Operation(operationId = "listerMesLignesFacturees",
            summary = "Les lignes article des pièces émises de l'opérateur dans une période, par nature (RG-FAC-34)")
    List<LigneVendueResponse> lignes(Connecte connecte, @PathVariable long operateur,
            @Parameter(description = "Premier jour, inclus") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
            @Parameter(description = "Dernier jour, inclus") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au,
            @Parameter(description = "Nature d'origine", example = "DSM") @RequestParam(required = false) String nature) {
        return mesFactures.lignes(Acteurs.de(connecte), operateur, du, au, texte(nature)).stream()
                .map(LigneVendueResponse::de).toList();
    }

    @GetMapping("/{id}")
    @Operation(operationId = "consulterMaFacture",
            summary = "Une pièce émise de l'opérateur ; l'ouverture est journalisée côté client (RG-FAC-28)")
    PieceResponse consulter(Connecte connecte, @PathVariable long operateur, @PathVariable long id) {
        return PieceResponse.de(mesFactures.consulter(Acteurs.de(connecte), operateur, id));
    }

    @GetMapping(path = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(operationId = "telechargerPdfMaFacture",
            summary = "Le PDF d'une pièce émise de l'opérateur, téléchargement journalisé côté client (RG-FAC-28)")
    ResponseEntity<byte[]> pdf(Connecte connecte, @PathVariable long operateur, @PathVariable long id) {
        DocumentLu pdf = mesFactures.pdf(Acteurs.de(connecte), operateur, id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(pdf.document().nom(), StandardCharsets.UTF_8).build().toString())
                .body(pdf.contenu());
    }

    private static Optional<String> texte(String valeur) {
        return Optional.ofNullable(valeur).map(String::strip).filter(v -> !v.isEmpty());
    }
}
