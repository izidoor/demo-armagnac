package fr.bnia.declarations.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Projection des factures en mémoire, idempotente comme l'adapter (même type, même numéro). */
final class FacturesEnMemoire implements FacturesDsm {

    private final List<FactureDsm> factures = new ArrayList<>();

    @Override
    public void inserer(FactureDsm facture) {
        if (factures.stream().noneMatch(f -> f.type() == facture.type() && f.numero().equals(facture.numero()))) {
            factures.add(facture);
        }
    }

    @Override
    public List<FactureDsm> deLaDsm(long dsm) {
        return factures.stream().filter(f -> f.dsm() == dsm)
                .sorted(Comparator.comparing(FactureDsm::date).thenComparing(FactureDsm::type)
                        .thenComparing(FactureDsm::numero))
                .toList();
    }
}
