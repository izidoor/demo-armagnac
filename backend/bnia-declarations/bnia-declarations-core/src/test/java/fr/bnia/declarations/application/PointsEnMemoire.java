package fr.bnia.declarations.application;

import fr.bnia.commun.domain.ModificationConcurrente;
import fr.bnia.declarations.domain.PointDeDepartDeclaratif;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** Points de départ en mémoire : attribue les identifiants et incrémente les versions. */
final class PointsEnMemoire implements PointDeDepartRepository {

    private final AtomicLong sequence = new AtomicLong(1);
    private final Map<Long, PointDeDepartDeclaratif> points = new LinkedHashMap<>();

    @Override
    public PointDeDepartDeclaratif inserer(PointDeDepartDeclaratif point) {
        return stocker(point, sequence.getAndIncrement(), 0);
    }

    @Override
    public PointDeDepartDeclaratif mettreAJour(PointDeDepartDeclaratif point) {
        if (points.get(point.id()).version() != point.version()) {
            throw new ModificationConcurrente(null, "modifié");
        }
        return stocker(point, point.id(), point.version() + 1);
    }

    private PointDeDepartDeclaratif stocker(PointDeDepartDeclaratif p, long id, long version) {
        points.put(id, PointDeDepartDeclaratif.reconstituer(id, p.operateur(), p.debut(), p.lignes(), p.fixePar(),
                p.fixeLe(), version));
        return parId(id).orElseThrow();
    }

    @Override
    public Optional<PointDeDepartDeclaratif> parId(long id) {
        return Optional.ofNullable(points.get(id)).map(p -> PointDeDepartDeclaratif.reconstituer(p.id(), p.operateur(),
                p.debut(), p.lignes(), p.fixePar(), p.fixeLe(), p.version()));
    }

    @Override
    public List<PointDeDepartDeclaratif> deLOperateur(long operateur) {
        return points.values().stream().filter(p -> p.operateur() == operateur)
                .sorted(Comparator.comparing(PointDeDepartDeclaratif::debut).reversed())
                .map(p -> parId(p.id()).orElseThrow())
                .toList();
    }

    @Override
    public Optional<PointDeDepartDeclaratif> enVigueur(long operateur) {
        return points.values().stream().filter(p -> p.operateur() == operateur)
                .max(Comparator.comparing(PointDeDepartDeclaratif::debut))
                .flatMap(p -> parId(p.id()));
    }
}
