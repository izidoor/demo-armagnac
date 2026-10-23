package fr.bnia.declarations.adapter.rest;

import fr.bnia.declarations.domain.IdentiteLigne;
import fr.bnia.declarations.domain.StockEstime;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "StockEstime",
        description = "Le stock estimé d'un opérateur : base (dernière DSM validée ou point de départ) vieillie à aujourd'hui, plus achats, moins ventes de ses retiraisons validées (spec 06 RG-CA-11)")
public record StockEstimeResponse(
        @Schema(description = "Date de la base ; absente sans DSM validée ni point de départ") LocalDate baseAu,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<LigneResponse> lignes) {

    @Schema(name = "LigneStockEstime")
    public record LigneResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) CleConditionnement cle,
            @Schema(description = "Lignes MB et MV") Integer millesime,
            @Schema(description = "Lignes NB et NV") String denomination,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "20+") String compte,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Litres d'alcool pur") BigDecimal base,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Litres d'alcool pur") BigDecimal achats,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Litres d'alcool pur") BigDecimal ventes,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Litres d'alcool pur") BigDecimal estime) {
    }

    static StockEstimeResponse depuis(StockEstime s) {
        return new StockEstimeResponse(s.baseAu().orElse(null), s.lignes().stream().map(l -> {
            IdentiteLigne i = l.identite();
            return new LigneResponse(i.cle(), i.millesime() == null ? null : i.millesime().annee(),
                    i.denomination() == null ? null : i.denomination().texte(), i.compte().toString(),
                    l.base().litres(), l.achats().litres(), l.ventes().litres(), l.estime().litres());
        }).toList());
    }
}
