package fr.bnia.declarations.adapter.rest;

import fr.bnia.declarations.application.ConsulterStocksDeclares.LigneStockListee;
import fr.bnia.declarations.application.StocksLecture.LigneStock;
import fr.bnia.declarations.domain.IdentiteLigne;
import fr.bnia.declarations.domain.TypeDeclarant;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "LigneStockDeclare",
        description = "Une ligne du stock déclaré d'un opérateur : le stock de fin d'une ligne de sa dernière DSM validée (RG-DEC-42)")
public record LigneStockDeclareResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long operateur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "N320001") String codeOperateur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String raisonSociale,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) TypeDeclarant type,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "La DSM dont la ligne est tirée") long dsm,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Fin de sa période : la date du stock")
        LocalDate au,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) CleConditionnement cle,
        @Schema(description = "Lignes MB et MV") Integer millesime,
        @Schema(description = "Lignes NB et NV") String denomination,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "20+") String compte,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Litres d'alcool pur") BigDecimal stock) {

    static LigneStockDeclareResponse depuis(LigneStockListee listee) {
        LigneStock l = listee.ligne();
        IdentiteLigne i = l.identite();
        return new LigneStockDeclareResponse(l.operateur(), listee.code(), listee.raisonSociale(), l.type(), l.dsm(),
                l.periode().fin(),
                i.cle(), i.millesime() == null ? null : i.millesime().annee(),
                i.denomination() == null ? null : i.denomination().texte(), i.compte().toString(), l.stock().litres());
    }
}
