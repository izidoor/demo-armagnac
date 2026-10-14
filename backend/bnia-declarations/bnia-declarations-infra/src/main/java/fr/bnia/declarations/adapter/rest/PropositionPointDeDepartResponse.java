package fr.bnia.declarations.adapter.rest;

import fr.bnia.declarations.adapter.rest.PointDeDepartResponse.LigneStockInitialResponse;
import fr.bnia.declarations.application.GererPointDeDepart.Proposition;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "PropositionPointDeDepart",
        description = "Le stock proposé pour un nouveau point de départ : le stock de fin de la dernière DSM validée, vieilli (RG-DEC-09)")
public record PropositionPointDeDepartResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Date de début retenue") LocalDate debut,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Période de la dernière DSM validée")
        PeriodeResponse derniere,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<LigneStockInitialResponse> lignes) {

    static PropositionPointDeDepartResponse depuis(Proposition p) {
        return new PropositionPointDeDepartResponse(p.debut(), PeriodeResponse.depuis(p.derniere()),
                PointDeDepartResponse.lignes(p.lignes()));
    }
}
