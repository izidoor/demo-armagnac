package fr.bnia.facturation.adapter.rest;

import fr.bnia.facturation.adapter.rest.PieceResponse.OrigineResponse;
import fr.bnia.facturation.api.Article;
import fr.bnia.facturation.api.CategorieTva;
import fr.bnia.facturation.api.LigneVendue;
import fr.bnia.facturation.api.NatureArticle;
import fr.bnia.facturation.application.Ventes;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "Ventes", description = "Les ventes consolidées d'une période, avoirs déduits (spec 08 RG-FAC-30)")
public record VentesResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate du,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate au,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<LigneVendueResponse> lignes) {

    @Schema(name = "LigneVendue",
            description = "Une ligne article d'une pièce émise, à sa date d'émission ; négative pour un avoir (RG-FAC-30)")
    public record LigneVendueResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long piece,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String numero,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean avoir,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate dateEmission,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String acheteur,
            @Schema(description = "Identifiant du client (opérateur) ; vide pour un client libre") String clientIdentifiant,
            @Schema(description = "N° BNIA du client") String clientCode,
            @Schema(description = "Vide pour un avoir") OrigineResponse origine,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "declarations") String source,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String code,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String designation,
            String unite,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) NatureArticle nature,
            String codeComptable,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) CategorieTva.Code tvaCode,
            @Schema(description = "Vide hors champ") BigDecimal tvaTaux,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "20,00 %") String tvaLibelle,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Signée, 4 décimales") BigDecimal quantite,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "HT, 4 décimales") BigDecimal prixUnitaire,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Signé") BigDecimal montantHt) {

        static LigneVendueResponse de(LigneVendue l) {
            return new LigneVendueResponse(l.piece(), l.numero(), l.avoir(), l.dateEmission(), l.acheteur(),
                    l.client() == null ? null : l.client().identifiant(), l.client() == null ? null : l.client().code(),
                    OrigineResponse.de(l.origine()), l.article().source(), l.article().code(), l.designation(),
                    l.unite(), l.nature(), l.codeComptable(), l.tva().code(), l.tva().taux(), l.tva().libelle(),
                    l.quantite(), l.prixUnitaire(), l.montantHt());
        }
    }

    @Schema(name = "ArticleCatalogue", description = "Un article d'un catalogue, à la date du jour (RG-FAC-29)")
    public record ArticleResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "declarations") String source,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String code,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String designation,
            String unite,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "HT, 4 décimales") BigDecimal prixUnitaire,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) CategorieTva.Code tvaCode,
            @Schema(description = "Vide hors champ") BigDecimal tvaTaux,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "20,00 %") String tvaLibelle,
            @Schema(description = "Motif d'exonération") String tvaMotif,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) NatureArticle nature,
            String codeComptable,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean actif,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "/bo/parametrage/taux-cvo",
                    description = "Chemin de l'écran où la source administre l'article") String lienAdministration) {

        static ArticleResponse de(Article a) {
            return new ArticleResponse(a.source(), a.code(), a.designation(), a.unite(), a.prixUnitaire(),
                    a.tva().code(), a.tva().taux(), a.tva().libelle(), a.tva().motif(), a.nature(), a.codeComptable(),
                    a.actif(), a.lienAdministration());
        }
    }

    static VentesResponse de(Ventes v) {
        return new VentesResponse(v.du(), v.au(), v.lignes().stream().map(LigneVendueResponse::de).toList());
    }
}
