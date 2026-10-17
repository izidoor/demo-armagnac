package fr.bnia.facturation.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.facturation.adapter.rest.VentesResponse.ArticleResponse;
import fr.bnia.facturation.application.ConsulterArticles;
import fr.bnia.facturation.application.ConsulterVentes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Le tableau des articles et les ventes consolidées, au back-office (UC-FAC-6, UC-FAC-8). */
@RestController
@RequestMapping("/api/facturation")
@Tag(name = "facturation")
class VentesController {

    private final ConsulterVentes ventes;
    private final ConsulterArticles articles;

    VentesController(ConsulterVentes ventes, ConsulterArticles articles) {
        this.ventes = ventes;
        this.articles = articles;
    }

    @GetMapping("/articles")
    @Operation(operationId = "listerArticles",
            summary = "Les articles de tous les catalogues à la date du jour, actifs ou non (RG-FAC-26, 29)")
    List<ArticleResponse> articles(Connecte connecte) {
        return articles.articles(Acteurs.de(connecte)).stream().map(ArticleResponse::de).toList();
    }

    @GetMapping("/ventes")
    @Operation(operationId = "consulterVentes",
            summary = "Les lignes vendues d'une période (date d'émission), avoirs déduits (RG-FAC-26, 30)")
    VentesResponse ventes(Connecte connecte,
            @Parameter(description = "Premier jour, inclus") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
            @Parameter(description = "Dernier jour, inclus") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) {
        return VentesResponse.de(ventes.ventes(Acteurs.de(connecte), du, au));
    }

    @GetMapping(path = "/ventes/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(operationId = "exporterVentesPdf",
            summary = "Le PDF des ventes d'une période : par article, récapitulatif de TVA, totaux (RG-FAC-30)")
    ResponseEntity<byte[]> pdf(Connecte connecte,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("ventes-" + du + "-" + au + ".pdf", StandardCharsets.UTF_8).build().toString())
                .body(ventes.pdf(Acteurs.de(connecte), du, au));
    }
}
