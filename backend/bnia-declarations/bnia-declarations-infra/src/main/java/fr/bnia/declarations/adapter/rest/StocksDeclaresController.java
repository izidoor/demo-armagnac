package fr.bnia.declarations.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.declarations.application.ConsulterStocksDeclares;
import fr.bnia.declarations.domain.AnneeDeclarative;
import fr.bnia.declarations.domain.TypeDeclarant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stocks déclarés (analyse UC-DEC-1, spec 05 §6.1, §6.2) : de tous, au back-office ; d'un opérateur, sur sa fiche
 * et au portail ; leur historique d'une année déclarative, pour le tableau croisé (RG-DEC-44).
 */
@RestController
@Tag(name = "declarations")
class StocksDeclaresController {

    private static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ConsulterStocksDeclares stocks;

    StocksDeclaresController(ConsulterStocksDeclares stocks) {
        this.stocks = stocks;
    }

    @GetMapping("/api/declarations/stocks")
    @Operation(operationId = "listerStocksDeclares",
            summary = "Les stocks déclarés de tous les opérateurs, filtrés par opérateur et type, ligne par ligne, par code d'opérateur (RG-DEC-01, 42)")
    List<LigneStockDeclareResponse> lister(Connecte connecte, @RequestParam(required = false) Long operateur,
            @RequestParam(required = false) TypeDeclarant type) {
        return stocks.deTous(Acteurs.de(connecte), Optional.ofNullable(operateur), Optional.ofNullable(type)).stream()
                .map(LigneStockDeclareResponse::depuis).toList();
    }

    @GetMapping("/api/declarations/stocks/historique")
    @Operation(operationId = "historiqueStocksDeclares",
            summary = "Les stocks déclarés de tous les opérateurs à chaque fin de mois écoulée d'une année déclarative (l'année en cours par défaut), filtrés par opérateur et type (RG-DEC-01, 44)")
    HistoriqueStocksResponse historique(Connecte connecte,
            @RequestParam(required = false) Integer annee, @RequestParam(required = false) Long operateur,
            @RequestParam(required = false) TypeDeclarant type) {
        return HistoriqueStocksResponse.depuis(stocks.historique(Acteurs.de(connecte), annee(annee),
                Optional.ofNullable(operateur), Optional.ofNullable(type)));
    }

    @GetMapping(path = "/api/declarations/stocks/excel", produces = XLSX)
    @Operation(operationId = "exporterStocksDeclares",
            summary = "L'état des stocks déclarés en Excel, pour les douanes, aux mêmes filtres que le listing (UC-DEC-1, RG-DEC-42)")
    ResponseEntity<byte[]> exporter(Connecte connecte, @RequestParam(required = false) Long operateur,
            @RequestParam(required = false) TypeDeclarant type) {
        byte[] contenu = stocks.exporter(Acteurs.de(connecte), Optional.ofNullable(operateur),
                Optional.ofNullable(type));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("stocks-declares-" + LocalDate.now() + ".xlsx", StandardCharsets.UTF_8).build()
                        .toString())
                .body(contenu);
    }

    @GetMapping("/api/operateurs/{operateur}/stock-declare")
    @Operation(operationId = "consulterStockDeclare",
            summary = "Le stock déclaré de l'opérateur : le stock de fin de sa dernière DSM validée ; 204 sans DSM validée (RG-DEC-01, 42)")
    ResponseEntity<StockDeclareResponse> consulter(Connecte connecte, @PathVariable long operateur) {
        return stocks.deLOperateur(Acteurs.de(connecte), operateur).map(StockDeclareResponse::depuis)
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/api/operateurs/{operateur}/stock-declare/historique")
    @Operation(operationId = "historiqueStockDeclare",
            summary = "Le stock déclaré de l'opérateur à chaque fin de mois écoulée d'une année déclarative, l'année en cours par défaut (RG-DEC-01, 44)")
    HistoriqueStocksResponse historiqueDe(Connecte connecte, @PathVariable long operateur,
            @RequestParam(required = false) Integer annee) {
        return HistoriqueStocksResponse.depuis(stocks.historiqueDe(Acteurs.de(connecte), operateur, annee(annee)));
    }

    /** Le paramètre {@code annee} : l'année de son 1er avril, 2026 pour 2026-2027. */
    private static Optional<AnneeDeclarative> annee(Integer annee) {
        return Optional.ofNullable(annee).map(AnneeDeclarative::new);
    }
}
