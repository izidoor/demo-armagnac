package fr.bnia.declarations.adapter.rest;

import fr.bnia.declarations.application.GererPointDeDepart.PointDeDepart;
import fr.bnia.declarations.domain.IdentiteLigne;
import fr.bnia.declarations.domain.LigneStockInitial;
import fr.bnia.declarations.domain.PointDeDepartDeclaratif;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "PointDeDepart", description = "Point de départ déclaratif : début et stock initial, fixés par le BNIA (spec 05 §3.3)")
public record PointDeDepartResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate debut,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<LigneStockInitialResponse> lignes,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Copie du nom de l'auteur") String fixePar,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant fixeLe,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Une DSM qui commence à son début est validée : il ne se modifie plus (RG-DEC-08)")
        boolean fige,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Figé et sans DSM en cours : un nouveau point de départ peut le remplacer (RG-DEC-09)")
        boolean renouvelable,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long version) {

    @Schema(name = "LigneStockInitial")
    public record LigneStockInitialResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) CleConditionnement cle,
            @Schema(description = "Lignes MB et MV") Integer millesime,
            @Schema(description = "Lignes NB et NV") String denomination,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "20+") String compte,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Litres d'alcool pur") BigDecimal volume) {
    }

    static PointDeDepartResponse depuis(PointDeDepart p) {
        PointDeDepartDeclaratif point = p.point();
        return new PointDeDepartResponse(point.id(), point.debut(), lignes(point.lignes()), point.fixePar().nom(),
                point.fixeLe(), p.fige(), p.renouvelable(), point.version());
    }

    static List<LigneStockInitialResponse> lignes(List<LigneStockInitial> lignes) {
        return lignes.stream().map(l -> {
            IdentiteLigne i = l.identite();
            return new LigneStockInitialResponse(i.cle(), i.millesime() == null ? null : i.millesime().annee(),
                    i.denomination() == null ? null : i.denomination().texte(), i.compte().toString(),
                    l.volume().litres());
        }).toList();
    }
}
