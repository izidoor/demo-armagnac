package fr.bnia.declarations.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.declarations.application.FacturerDsm;
import fr.bnia.declarations.application.FiltreDsm;
import fr.bnia.declarations.application.ListerDsm;
import fr.bnia.declarations.domain.TypeDeclarant;
import fr.bnia.declarations.domain.partage.EtatDsm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Listing de toutes les DSM, au back-office (analyse UC-DEC-7, spec 05 §6.1). */
@RestController
@Tag(name = "declarations")
class ListingDsmController {

    private final ListerDsm lister;
    private final FacturerDsm facturer;

    ListingDsmController(ListerDsm lister, FacturerDsm facturer) {
        this.lister = lister;
        this.facturer = facturer;
    }

    @Schema(name = "FacturationDsm", description = "Les DSM à facturer ensemble (RG-DEC-43)")
    record FacturationRequest(@Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<Long> dsms) {
    }

    @Schema(name = "DsmFacturee", description = "La facture demandée d'une DSM : son brouillon chez facturation")
    record DsmFactureeResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long dsm,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Identifiant de la pièce")
            long brouillon) {
    }

    @PostMapping("/api/declarations/facturation")
    @Operation(operationId = "facturerSelectionDsm",
            summary = "Le BNIA demande les factures d'une sélection de DSM validées, toutes ou aucune (RG-DEC-43)")
    List<DsmFactureeResponse> facturer(Connecte connecte, @RequestBody FacturationRequest requete) {
        return facturer.facturer(Acteurs.de(connecte), requete.dsms()).stream()
                .map(f -> new DsmFactureeResponse(f.dsm(), f.brouillon())).toList();
    }

    @GetMapping("/api/declarations/dsm")
    @Operation(operationId = "listerToutesDsm",
            summary = "Les DSM de tous les opérateurs, filtrées par opérateur, type, mois et états, de la période la plus récente à la plus ancienne (RG-DEC-01)")
    List<DsmListeeResponse> lister(Connecte connecte,
            @RequestParam(required = false) Long operateur,
            @RequestParam(required = false) TypeDeclarant type,
            @Parameter(description = "Les DSM dont la période contient ce mois", example = "2026-10")
            @RequestParam(required = false) YearMonth mois,
            @Parameter(description = "Les DSM dans l'un de ces états ; aucun : tous")
            @RequestParam(required = false) List<EtatDsm> etats) {
        FiltreDsm filtre = new FiltreDsm(Optional.ofNullable(operateur), Optional.ofNullable(type),
                Optional.ofNullable(mois), etats == null ? Set.of() : Set.copyOf(etats));
        return lister.lister(Acteurs.de(connecte), filtre).stream().map(DsmListeeResponse::depuis).toList();
    }
}
