package fr.bnia.declarations.adapter.rest;

import fr.bnia.declarations.application.ConsulterStocksDeclares.HistoriqueStocks;
import fr.bnia.declarations.application.ConsulterStocksDeclares.StockALaDate;
import fr.bnia.declarations.application.StocksLecture.LigneStock;
import fr.bnia.declarations.domain.AnneeDeclarative;
import fr.bnia.declarations.domain.IdentiteLigne;
import fr.bnia.declarations.domain.TypeDeclarant;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "HistoriqueStocks",
        description = "Les stocks déclarés d'une année déclarative, à chaque fin de mois écoulée (RG-DEC-44)")
public record HistoriqueStocksResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Annee annee,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Les années qui ont une DSM validée, et l'année en cours, de la plus récente à la plus ancienne")
        List<Annee> anneesDisponibles,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<Ligne> lignes) {

    @Schema(name = "AnneeDeclarative", description = "Du 1er avril au 31 mars (RG-DEC-44)")
    public record Annee(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Année de son 1er avril", example = "2026")
            int debutAnnee,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-2027") String libelle,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate debut,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate fin) {

        static Annee depuis(AnneeDeclarative a) {
            return new Annee(a.debutAnnee(), a.libelle(), a.debut(), a.fin());
        }
    }

    @Schema(name = "StockALaDate",
            description = "Une ligne du stock déclaré d'un opérateur à une fin de mois : celle de sa dernière DSM validée finie à cette date (RG-DEC-44)")
    public record Ligne(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "La fin de mois") LocalDate au,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long operateur,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "N320001") String codeOperateur,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String raisonSociale,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) TypeDeclarant type,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "La DSM dont la ligne est tirée") long dsm,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "juillet 2026") String periodeDsm,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Fin de la période de la DSM")
            LocalDate finDsm,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) CleConditionnement cle,
            @Schema(description = "Lignes MB et MV") Integer millesime,
            @Schema(description = "Lignes NB et NV") String denomination,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "20+") String compte,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Litres d'alcool pur") BigDecimal stock) {

        static Ligne depuis(StockALaDate s) {
            LigneStock l = s.ligne().ligne();
            IdentiteLigne i = l.identite();
            return new Ligne(s.au(), l.operateur(), s.ligne().code(), s.ligne().raisonSociale(), l.type(), l.dsm(),
                    l.periode().libelle(), l.periode().fin(), i.cle(),
                    i.millesime() == null ? null : i.millesime().annee(),
                    i.denomination() == null ? null : i.denomination().texte(), i.compte().toString(),
                    l.stock().litres());
        }
    }

    static HistoriqueStocksResponse depuis(HistoriqueStocks h) {
        return new HistoriqueStocksResponse(Annee.depuis(h.annee()),
                h.anneesDisponibles().stream().map(Annee::depuis).toList(),
                h.lignes().stream().map(Ligne::depuis).toList());
    }
}
