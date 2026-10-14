package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.CalendrierDeclaratif.DsmPassee;
import fr.bnia.declarations.domain.PointDeDepartDeclaratif;
import fr.bnia.declarations.domain.partage.EtatDsm;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** Les repères du calendrier, tirés des points de départ et des DSM en mémoire, comme l'adapter. */
final class CalendriersEnMemoire implements CalendriersLecture {

    private final PointsEnMemoire points;
    private final DsmEnMemoire dsms;

    CalendriersEnMemoire(PointsEnMemoire points, DsmEnMemoire dsms) {
        this.points = points;
        this.dsms = dsms;
    }

    @Override
    public Map<Long, Reperes> reperes() {
        Map<Long, Reperes> reperes = new HashMap<>();
        for (long operateur : List.of(Monde.NEGOCIANT, Monde.PRODUCTEUR, Monde.SOMMEIL)) {
            points.enVigueur(operateur).map(PointDeDepartDeclaratif::debut).ifPresent(debut -> {
                List<DsmPassee> toutes = dsms.dsmsDe(operateur);
                Stream<DsmPassee> nonValidee = toutes.stream().filter(d -> d.etat() != EtatDsm.VALIDEE);
                Stream<DsmPassee> derniere = toutes.stream()
                        .filter(d -> d.etat() == EtatDsm.VALIDEE && !d.periode().debut().isBefore(debut))
                        .max(Comparator.comparing(d -> d.periode().debut())).stream();
                reperes.put(operateur, new Reperes(debut, Stream.concat(nonValidee, derniere).toList()));
            });
        }
        return reperes;
    }
}
