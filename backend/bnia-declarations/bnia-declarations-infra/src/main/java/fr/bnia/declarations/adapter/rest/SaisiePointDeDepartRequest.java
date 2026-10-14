package fr.bnia.declarations.adapter.rest;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.CleConditionnement;
import fr.bnia.declarations.domain.SaisieLigneStock;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "SaisiePointDeDepart", description = "Fixer ou modifier un point de départ (RG-DEC-06 à 08)")
public record SaisiePointDeDepartRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Un 1er avril pour un producteur, un premier du mois pour un négociant") LocalDate debut,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<SaisieLigneStockRequest> lignes,
        @Schema(description = "Version lue, à la modification (verrou optimiste)") Long version) {

    @Schema(name = "SaisieLigneStockInitial")
    public record SaisieLigneStockRequest(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) CleConditionnement cle,
            @Schema(description = "Lignes MB et MV") Integer millesime,
            @Schema(description = "Lignes NB et NV") String denomination,
            @Schema(description = "Ignoré pour une ligne MB, dont le compte est calculé", example = "12") String compte,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Litres d'alcool pur") BigDecimal volume) {
    }

    List<SaisieLigneStock> versSaisie() {
        return lignes == null ? List.of() : lignes.stream().map(l -> new SaisieLigneStock(l.cle(), l.millesime(),
                l.denomination(), l.compte(), l.volume() == null ? null : VolumeAlcoolPur.litres(l.volume()))).toList();
    }
}
