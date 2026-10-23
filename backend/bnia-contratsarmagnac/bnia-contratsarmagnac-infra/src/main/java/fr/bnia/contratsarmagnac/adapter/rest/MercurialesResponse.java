package fr.bnia.contratsarmagnac.adapter.rest;

import fr.bnia.contratsarmagnac.application.ConsulterContrats.Mercuriales;
import fr.bnia.contratsarmagnac.domain.Mercuriale;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "Mercuriales", description = "Les prix des contrats conclus ou exécutés d'une période, par produit (spec 06 RG-CA-26)")
public record MercurialesResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate du,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate au,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Les millésimes du plus récent au plus ancien, puis les assemblages")
        List<ProduitResponse> produits) {

    @Schema(name = "MercurialeProduit")
    public record ProduitResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Millésime 2015") String libelle,
            Integer millesime,
            String denomination,
            @Schema(description = "Assemblage seulement", example = "20+") String compte,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int contrats,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Litres d'alcool pur") BigDecimal volumeAp,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "€ HT / hl AP") BigDecimal prixMinimum,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "€ HT / hl AP, pondéré par le volume AP")
            BigDecimal prixMoyen,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "€ HT / hl AP") BigDecimal prixMaximum) {

        static ProduitResponse depuis(Mercuriale m) {
            return new ProduitResponse(m.libelle(), m.millesime(), m.denomination(),
                    m.compte() == null ? null : m.compte().toString(), m.contrats(), m.volumeAp().litres(),
                    m.prixMinimum(), m.prixMoyen(), m.prixMaximum());
        }
    }

    static MercurialesResponse depuis(Mercuriales m) {
        return new MercurialesResponse(m.du(), m.au(), m.produits().stream().map(ProduitResponse::depuis).toList());
    }
}
