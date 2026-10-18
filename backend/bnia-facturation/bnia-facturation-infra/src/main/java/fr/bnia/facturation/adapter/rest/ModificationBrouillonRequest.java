package fr.bnia.facturation.adapter.rest;

import fr.bnia.facturation.adapter.rest.PieceResponse.LigneResponse.Sorte;
import fr.bnia.facturation.api.ArticleRef;
import fr.bnia.facturation.api.ConditionPaiement;
import fr.bnia.facturation.application.ModifierBrouillon.LigneSaisie;
import fr.bnia.facturation.application.ModifierBrouillon.LigneSaisie.ArticleSaisi;
import fr.bnia.facturation.application.ModifierBrouillon.LigneSaisie.GroupeSaisi;
import fr.bnia.facturation.application.ModifierBrouillon.LigneSaisie.TexteSaisi;
import fr.bnia.facturation.application.ModifierBrouillon.Saisie;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(name = "ModificationBrouillon",
        description = "Un brouillon tel que saisi, enregistré d'un coup ; date de l'opération et acheteur n'en sont pas (RG-FAC-17)")
public record ModificationBrouillonRequest(
        String libellePeriode,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) ConditionRequest condition,
        String bonDeCommande,
        String noteEntete,
        String notePied,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Dans l'ordre d'impression")
        List<LigneSaisieRequest> lignes,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Version de la pièce affichée")
        long version) {

    @Schema(name = "ConditionPaiementSaisie", description = "Condition de paiement (spec 08 §3.6, RG-FAC-13)")
    public record ConditionRequest(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "De 0 à 60 ; 45 au plus avec fin de mois")
            int jours,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean finDeMois) {
    }

    @Schema(name = "LigneSaisie", description = "Une ligne saisie ; numéro vide pour une ligne nouvelle (RG-FAC-17)")
    public record LigneSaisieRequest(
            @Schema(description = "Numéro d'une ligne existante ; vide pour une ligne nouvelle") Integer numero,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Sorte sorte,
            @Schema(description = "Désignation d'un article (facultative pour un article nouveau), texte, ou titre d'un groupe")
            String texte,
            @Schema(description = "Article nouveau : source") String source,
            @Schema(description = "Article nouveau : code dans sa source") String code,
            @Schema(description = "Article") String unite,
            @Schema(description = "Article : non nulle (RG-FAC-06)") BigDecimal quantite,
            @Schema(description = "Article : HT ; facultatif pour un article nouveau (celui du catalogue)")
            BigDecimal prixUnitaire,
            @Schema(description = "Groupe : ses lignes") List<LigneSaisieRequest> lignes) {

        LigneSaisie versSaisie() {
            return switch (sorte) {
                case ARTICLE -> new ArticleSaisi(numero, numero == null ? new ArticleRef(source, code) : null, texte,
                        unite, quantite, prixUnitaire);
                case TEXTE -> new TexteSaisi(numero, texte);
                case GROUPE -> new GroupeSaisi(numero, texte,
                        lignes == null ? List.of() : lignes.stream().map(LigneSaisieRequest::versSaisie).toList());
            };
        }
    }

    Saisie versSaisie() {
        return new Saisie(libellePeriode, condition == null ? null
                : new ConditionPaiement(condition.jours(), condition.finDeMois()), bonDeCommande, noteEntete, notePied,
                lignes == null ? List.of() : lignes.stream().map(LigneSaisieRequest::versSaisie).toList(), version);
    }
}
