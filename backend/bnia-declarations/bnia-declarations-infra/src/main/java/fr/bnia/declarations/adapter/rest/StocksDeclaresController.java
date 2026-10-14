package fr.bnia.declarations.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.declarations.application.ConsulterStocksDeclares;
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

/** Stocks déclarés (analyse UC-DEC-1, spec 05 §6.1) : de tous, au back-office ; d'un opérateur, sur sa fiche. */
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
}
