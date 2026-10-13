package fr.bnia.declarations.adapter.rest;

import fr.bnia.declarations.application.CotisationDsm;
import fr.bnia.declarations.application.CotisationDsm.Calculee;
import fr.bnia.declarations.application.CotisationDsm.Facturee;
import fr.bnia.declarations.application.CotisationDsm.NonCalculable;
import fr.bnia.declarations.application.FactureDsm;
import fr.bnia.declarations.domain.GrilleCotisation.TauxEnVigueur;
import fr.bnia.declarations.domain.PolitiqueCotisation.Detail;
import fr.bnia.declarations.domain.PolitiqueCotisation.LigneDetail;
import fr.bnia.declarations.domain.partage.LigneCotisation;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "CotisationDsm", description = "Cotisation CVO d'une DSM (spec 05 §6.3, RG-DEC-31 à 34)")
public record CotisationResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Statut statut,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Fin de la période, date du taux appliqué (RG-DEC-32)")
        LocalDate finPeriode,
        @Schema(description = "Calculée : le taux en vigueur à la fin de la période") TauxCvoResponse taux,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Calculée : une ligne par ligne de cotisation non nulle")
        List<LigneResponse> lignes,
        @Schema(description = "Calculée : somme des parts non assujetties") BigDecimal totalNonAssujettie,
        @Schema(description = "Calculée : somme des parts assujetties") BigDecimal totalAssujettie,
        @Schema(description = "Calculée : somme des parts ; facturée : factures moins avoirs ; absent si non calculable")
        BigDecimal totalHt,
        @Schema(description = "Calculée : TVA indicative, absente si la classe de TVA de la CVO n'a pas de taux")
        TvaResponse tva,
        @Schema(description = "Calculée : HT + TVA indicative") BigDecimal ttc,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Facturée : factures et avoirs, par date")
        List<FactureResponse> factures) {

    public enum Statut {
        CALCULEE,
        NON_CALCULABLE,
        FACTUREE
    }

    @Schema(name = "LigneCotisation", description = "Une ligne du détail de la cotisation (RG-DEC-33)")
    public record LigneResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LigneCotisation ligne,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Sorties France") String libelle,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Litres d'alcool pur", example = "839.11")
            BigDecimal volume,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "hlAP", example = "8.3911")
            BigDecimal hectolitres,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "100.69") BigDecimal nonAssujettie,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "402.77") BigDecimal assujettie,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "503.46") BigDecimal total) {
    }

    @Schema(name = "TvaIndicative", description = "TVA indicative d'une DSM non facturée (RG-DEC-34, H-DEC-22)")
    public record TvaResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Pourcentage", example = "20.00")
            BigDecimal taux,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "296.95") BigDecimal montant) {
    }

    @Schema(name = "FactureDsm", description = "Facture ou avoir d'une DSM, reçu de facturation (A-DEC-4)")
    public record FactureResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) FactureDsm.Type type,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String numero,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate date,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal ht,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal tva,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal ttc) {
    }

    static CotisationResponse depuis(CotisationDsm cotisation, LocalDate finPeriode) {
        return switch (cotisation) {
            case Calculee c -> calculee(c, finPeriode);
            case NonCalculable n -> new CotisationResponse(Statut.NON_CALCULABLE, finPeriode, null, List.of(), null,
                    null, null, null, null, List.of());
            case Facturee f -> new CotisationResponse(Statut.FACTUREE, finPeriode, null, List.of(), null, null,
                    f.totalHt(), null, null, f.documents().stream()
                    .map(d -> new FactureResponse(d.type(), d.numero(), d.date(), d.ht(), d.tva(), d.ttc())).toList());
        };
    }

    private static CotisationResponse calculee(Calculee c, LocalDate finPeriode) {
        Detail d = c.detail();
        return new CotisationResponse(Statut.CALCULEE, finPeriode, TauxCvoResponse.depuis(d.taux()),
                d.lignes().stream().map(CotisationResponse::ligne).toList(), d.totalNonAssujettie(),
                d.totalAssujettie(), d.totalHt(), c.tva().map(t -> new TvaResponse(t.taux(), t.montant())).orElse(null),
                c.ttc().orElse(null), List.of());
    }

    private static LigneResponse ligne(LigneDetail l) {
        return new LigneResponse(l.ligne(), l.ligne().libelle(), l.volume().litres(), l.hectolitres(), l.nonAssujettie(),
                l.assujettie(), l.total());
    }

    /** Un taux de la grille et sa fin calculée (RG-DEC-30). */
    @Schema(name = "TauxCvo", description = "Taux de la grille CVO, avec sa fin calculée (spec 05 §3.5, RG-DEC-30)")
    public record TauxCvoResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "€/hlAP", example = "60.00") BigDecimal montant,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "20.00") BigDecimal pourcentageExonere,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate debut,
            @Schema(description = "Veille du début du taux suivant ; absente pour le dernier") LocalDate fin) {

        static TauxCvoResponse depuis(TauxEnVigueur t) {
            return new TauxCvoResponse(t.taux().montant(), t.taux().pourcentageExonere(), t.taux().debut(),
                    t.fin().orElse(null));
        }
    }
}
