package fr.bnia.facturation.adapter.rest;

import fr.bnia.facturation.api.Article;
import fr.bnia.facturation.api.CategorieTva;
import fr.bnia.facturation.api.NatureArticle;
import fr.bnia.facturation.application.EmettreLot.CompteRendu;
import fr.bnia.facturation.application.TransmettreAZeendoc;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

/** Les corps et réponses du travail sur les brouillons (lot 2 de la spec 08). */
public final class BrouillonsRequests {

    private BrouillonsRequests() {
    }

    @Schema(name = "EmissionBrouillons", description = "Les brouillons à émettre, dans l'ordre de la sélection (RG-FAC-19)")
    public record EmissionRequest(@Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<Long> ids) {
    }

    @Schema(name = "CompteRenduEmission",
            description = "Le sort d'un brouillon de la sélection : son numéro, ou le motif du refus (RG-FAC-19)")
    public record CompteRenduResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
            @Schema(description = "Vide si le brouillon a été refusé") String numero,
            @Schema(description = "Vide s'il a été émis") String motif) {

        static CompteRenduResponse de(CompteRendu c) {
            return new CompteRenduResponse(c.id(), c.numero(), c.motif());
        }
    }

    @Schema(name = "CompteRenduTransmission",
            description = "Le sort d'une pièce de la sélection transmise à Zeendoc : transmise, ou le motif du refus (RG-FAC-32)")
    public record CompteRenduTransmissionResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
            @Schema(description = "Vide si la pièce est introuvable") String numero,
            @Schema(description = "Vide si elle a été transmise") String motif) {

        static CompteRenduTransmissionResponse de(TransmettreAZeendoc.CompteRendu c) {
            return new CompteRenduTransmissionResponse(c.id(), c.numero(), c.motif());
        }
    }

    @Schema(name = "NotePiece", description = "Une note interne, jamais imprimée ni transmise (RG-FAC-21)")
    public record NoteRequest(@Schema(requiredMode = Schema.RequiredMode.REQUIRED) String texte) {
    }

    @Schema(name = "ArticlePropose",
            description = "Un article actif d'un catalogue, à la date de l'opération du brouillon (RG-FAC-03)")
    public record ArticleProposeResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "declarations") String source,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String code,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String designation,
            String unite,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "HT, 4 décimales") BigDecimal prixUnitaire,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) CategorieTva.Code tvaCode,
            @Schema(description = "Vide hors champ") BigDecimal tvaTaux,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "20,00 %") String tvaLibelle,
            @Schema(description = "Motif d'exonération") String tvaMotif,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) NatureArticle nature) {

        static ArticleProposeResponse de(Article a) {
            return new ArticleProposeResponse(a.source(), a.code(), a.designation(), a.unite(), a.prixUnitaire(),
                    a.tva().code(), a.tva().taux(), a.tva().libelle(), a.tva().motif(), a.nature());
        }
    }
}
