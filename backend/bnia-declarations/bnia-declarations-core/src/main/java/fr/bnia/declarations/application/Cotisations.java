package fr.bnia.declarations.application;

import fr.bnia.declarations.application.CotisationDsm.Calculee;
import fr.bnia.declarations.application.CotisationDsm.Facturee;
import fr.bnia.declarations.application.CotisationDsm.NonCalculable;
import fr.bnia.declarations.application.CotisationDsm.TvaIndicative;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.GrilleCotisation;
import fr.bnia.declarations.domain.PolitiqueCotisation;
import fr.bnia.declarations.domain.PolitiqueCotisation.Detail;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * La cotisation d'une DSM (spec 05 §3.6, RG-DEC-31 à 34) : calculée à la volée tant qu'elle n'est pas facturée,
 * avec la TVA indicative ; facturée, ses factures. Partagé par les cas d'usage du contexte.
 */
final class Cotisations {

    private static final BigDecimal CENT = BigDecimal.valueOf(100);

    private final GrilleCotisationRepository grilles;
    private final FacturesDsm factures;
    private final ReferentielsLecture referentiels;
    private final ParametresDeclarations parametres;
    private final Clock horloge;

    Cotisations(GrilleCotisationRepository grilles, FacturesDsm factures, ReferentielsLecture referentiels,
            ParametresDeclarations parametres, Clock horloge) {
        this.grilles = grilles;
        this.factures = factures;
        this.referentiels = referentiels;
        this.parametres = parametres;
        this.horloge = horloge;
    }

    /** RG-DEC-32 à 34. */
    CotisationDsm de(DeclarationStocksMouvements dsm) {
        List<FactureDsm> documents = dsm.id() == null ? List.of() : factures.deLaDsm(dsm.id());
        if (documents.stream().anyMatch(f -> f.type() == FactureDsm.Type.FACTURE)) {
            return new Facturee(documents.stream().filter(f -> f.type() != FactureDsm.Type.BROUILLON).toList());
        }
        return PolitiqueCotisation.calculer(dsm.type(), dsm.periode().fin(), dsm::total, grilles.charger())
                .<CotisationDsm>map(detail -> new Calculee(detail, tva(detail)))
                .orElseGet(() -> new NonCalculable(dsm.periode().fin()));
    }

    /** RG-DEC-43 : la pièce de {@code facturation} dont la facture est demandée, pas encore émise. */
    Optional<Long> brouillon(DeclarationStocksMouvements dsm) {
        return dsm.id() == null ? Optional.empty() : factures.deLaDsm(dsm.id()).stream()
                .filter(f -> f.type() == FactureDsm.Type.BROUILLON).map(FactureDsm::piece).findFirst();
    }

    /** La grille, à charger une fois pour tout un listing. */
    GrilleCotisation grille() {
        return grilles.charger();
    }

    /** RG-DEC-34 : le total HT d'une DSM du listing, facturé ou calculé ; absent si non calculable. */
    static Optional<BigDecimal> totalHt(DsmLigne ligne, GrilleCotisation grille) {
        if (ligne.htFacture().isPresent()) {
            return ligne.htFacture();
        }
        return PolitiqueCotisation.calculer(ligne.type(), ligne.periode().fin(), ligne::total, grille)
                .map(Detail::totalHt);
    }

    /** RG-DEC-34, H-DEC-22 : au taux de la classe de TVA de la CVO à la date du jour. */
    private Optional<TvaIndicative> tva(Detail detail) {
        LocalDate aujourdhui = LocalDate.now(horloge);
        return referentiels.classeTvaParCode(parametres.classeTvaCvo())
                .flatMap(classe -> referentiels.tauxTva(classe.id(), aujourdhui))
                .map(taux -> new TvaIndicative(taux.taux(), PolitiqueCotisation.centimes(
                        detail.totalAssujettie().multiply(taux.taux()).divide(CENT))));
    }
}
