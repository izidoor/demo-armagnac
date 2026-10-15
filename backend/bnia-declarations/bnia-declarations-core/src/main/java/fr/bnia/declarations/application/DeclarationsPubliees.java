package fr.bnia.declarations.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.declarations.application.partage.DeclarationsLecture;
import fr.bnia.declarations.domain.IdentiteLigne;
import fr.bnia.declarations.domain.partage.DerniereDsmVue;
import fr.bnia.declarations.domain.partage.StockDeclareVue;
import fr.bnia.declarations.domain.partage.StockDeclareVue.LigneStockVue;
import java.util.Optional;

/**
 * La lecture publiée des déclarations (spec 05 §5.2) : la dernière DSM et le stock déclaré d'un opérateur, pour
 * sa fiche. La cotisation n'y est plus : {@code declarations} demande elle-même ses factures (spec 08 §3.13).
 */
@UseCase
public class DeclarationsPubliees implements DeclarationsLecture {

    private final DsmLecture lecture;
    private final StocksLecture stocks;

    public DeclarationsPubliees(DsmLecture lecture, StocksLecture stocks) {
        this.lecture = lecture;
        this.stocks = stocks;
    }

    @Override
    public Optional<DerniereDsmVue> derniereDsm(long operateur) {
        return lecture.deLOperateur(operateur).stream().findFirst()
                .map(d -> new DerniereDsmVue(d.id(), d.operateur(), d.periode().debut(), d.periode().fin(), d.etat()));
    }

    @Override
    public Optional<StockDeclareVue> stockDeclare(long operateur) {
        return ConsulterStocksDeclares.stockDe(lecture, stocks, operateur).map(s -> new StockDeclareVue(operateur,
                s.dsm(), s.periode().fin(), s.lignes().stream().map(l -> {
                    IdentiteLigne i = l.identite();
                    return new LigneStockVue(i.cle(), i.millesime() == null ? null : i.millesime().annee(),
                            i.denomination() == null ? null : i.denomination().texte(), i.compte(), l.stock());
                }).toList()));
    }
}
