package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.LigneStockInitial;
import fr.bnia.declarations.domain.MouvementContrat;
import fr.bnia.declarations.domain.PointDeDepartDeclaratif;
import fr.bnia.declarations.domain.StockEstime;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Calcul du stock estimé d'un opérateur (spec 06 RG-CA-11), à la lecture : partagé par la lecture publiée et par la
 * page « Stocks déclarés » du portail. Sans contrôle de droits : l'appelant vérifie les siens.
 */
final class StocksEstimes {

    private final DsmLecture dsms;
    private final StocksLecture stocks;
    private final PointDeDepartRepository points;
    private final MouvementsContrat mouvements;
    private final Clock horloge;

    StocksEstimes(DsmLecture dsms, StocksLecture stocks, PointDeDepartRepository points, MouvementsContrat mouvements,
            Clock horloge) {
        this.dsms = dsms;
        this.stocks = stocks;
        this.points = points;
        this.mouvements = mouvements;
        this.horloge = horloge;
    }

    /**
     * RG-CA-11 : la base est le stock de fin de la dernière DSM validée (RG-DEC-42), sinon le stock initial du point
     * de départ en vigueur, s'il est postérieur à cette DSM (reprise après interruption) ; sans l'une ni l'autre, nulle.
     * S'y ajoutent les mouvements de contrat postérieurs à la base.
     */
    StockEstime de(long operateur) {
        LocalDate aujourdhui = LocalDate.now(horloge);
        List<MouvementContrat> contrats = mouvements.deLOperateur(operateur);
        Optional<ConsulterStocksDeclares.StockDeclare> declare = ConsulterStocksDeclares.stockDe(dsms, stocks, operateur);
        Optional<PointDeDepartDeclaratif> point = points.enVigueur(operateur);
        if (declare.isPresent()
                && point.map(p -> !p.debut().isAfter(declare.get().periode().fin())).orElse(true)) {
            ConsulterStocksDeclares.StockDeclare d = declare.get();
            LocalDate fin = d.periode().fin();
            return StockEstime.depuis(d.lignes().stream()
                    .filter(l -> !l.stock().estNegatif())
                    .map(l -> new LigneStockInitial(l.identite(), l.stock()))
                    .toList(), fin, fin.plusDays(1), aujourdhui, contrats);
        }
        return point.map(p -> StockEstime.depuis(p.lignes(), p.debut(), p.debut().plusDays(1), aujourdhui, contrats))
                .orElseGet(() -> StockEstime.sansBase(aujourdhui, contrats));
    }
}
