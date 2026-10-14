package fr.bnia.declarations.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.declarations.application.partage.DeclarationsLecture;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.EtatDsm;
import fr.bnia.declarations.domain.PolitiqueCotisation;
import fr.bnia.declarations.domain.PolitiqueCotisation.Detail;
import fr.bnia.declarations.domain.partage.CotisationDsmVue;
import fr.bnia.declarations.domain.partage.CotisationDsmVue.LigneCotisationVue;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** La lecture publiée des déclarations (spec 05 §5.2) : la cotisation d'une DSM, pour {@code facturation}. */
@UseCase
public class CotisationsPubliees implements DeclarationsLecture {

    private final DsmRepository dsms;
    private final GrilleCotisationRepository grilles;
    private final FacturesDsm factures;

    public CotisationsPubliees(DsmRepository dsms, GrilleCotisationRepository grilles, FacturesDsm factures) {
        this.dsms = dsms;
        this.grilles = grilles;
        this.factures = factures;
    }

    @Override
    public Optional<CotisationDsmVue> cotisation(long id) {
        return dsms.parId(id).map(dsm -> {
            Optional<Detail> detail = PolitiqueCotisation.calculer(dsm.type(), dsm.periode().fin(), dsm::total,
                    grilles.charger());
            boolean facturee = factures.deLaDsm(id).stream().anyMatch(f -> f.type() == FactureDsm.Type.FACTURE);
            return vue(dsm, facturee, detail);
        });
    }

    private static CotisationDsmVue vue(DeclarationStocksMouvements dsm, boolean facturee, Optional<Detail> detail) {
        List<LigneCotisationVue> lignes = detail.map(d -> d.lignes().stream()
                .map(l -> new LigneCotisationVue(l.ligne(), l.hectolitres(), l.nonAssujettie(), l.assujettie()))
                .toList()).orElse(List.of());
        return new CotisationDsmVue(dsm.id(), dsm.operateur(), dsm.etat() == EtatDsm.VALIDEE, facturee,
                dsm.periode().fin(), detail.map(d -> d.taux().taux().montant()),
                detail.map(d -> d.taux().taux().pourcentageExonere()), lignes,
                detail.map(Detail::totalHt).orElse(BigDecimal.ZERO.setScale(2)));
    }
}
