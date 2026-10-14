package fr.bnia.declarations.adapter.rest;

import fr.bnia.declarations.application.ConsulterStocksDeclares.StockDeclare;
import fr.bnia.declarations.application.StocksLecture.LigneStock;
import fr.bnia.declarations.domain.IdentiteLigne;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Schema(name = "StockDeclare",
        description = "Le stock déclaré d'un opérateur : le stock de fin de sa dernière DSM validée, ligne par ligne, lignes nulles omises (RG-DEC-42)")
public record StockDeclareResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "La DSM dont il est tiré") long dsm,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Sa période : le stock est daté de sa fin")
        PeriodeResponse periode,
        Instant valideeLe,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<LigneResponse> lignes) {

    @Schema(name = "LigneStock")
    public record LigneResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) CleConditionnement cle,
            @Schema(description = "Lignes MB et MV") Integer millesime,
            @Schema(description = "Lignes NB et NV") String denomination,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "20+") String compte,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Litres d'alcool pur") BigDecimal stock) {
    }

    static StockDeclareResponse depuis(StockDeclare s) {
        return new StockDeclareResponse(s.dsm(), PeriodeResponse.depuis(s.periode()), s.valideeLe(),
                s.lignes().stream().map(StockDeclareResponse::ligne).toList());
    }

    private static LigneResponse ligne(LigneStock l) {
        IdentiteLigne i = l.identite();
        return new LigneResponse(i.cle(), i.millesime() == null ? null : i.millesime().annee(),
                i.denomination() == null ? null : i.denomination().texte(), i.compte().toString(), l.stock().litres());
    }
}
