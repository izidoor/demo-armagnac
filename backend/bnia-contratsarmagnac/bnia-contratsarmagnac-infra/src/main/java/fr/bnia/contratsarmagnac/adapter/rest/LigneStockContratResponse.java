package fr.bnia.contratsarmagnac.adapter.rest;

import fr.bnia.declarations.domain.partage.CleConditionnement;
import fr.bnia.declarations.domain.partage.StockEstimeVue.LigneStockEstimeVue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "LigneStockContrat",
        description = "Une ligne du stock estimé sous bois du vendeur, où il choisit les lignes de son contrat (RG-CA-11, 12)")
public record LigneStockContratResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) CleConditionnement cle,
        @Schema(description = "Lignes MB") Integer millesime,
        @Schema(description = "Lignes NB") String denomination,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "20+") String compte,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Stock estimé, litres d'alcool pur")
        BigDecimal estime) {

    static LigneStockContratResponse depuis(LigneStockEstimeVue l) {
        return new LigneStockContratResponse(l.cle(), l.millesime(), l.denomination(), l.compte().toString(),
                l.estime().litres());
    }
}
