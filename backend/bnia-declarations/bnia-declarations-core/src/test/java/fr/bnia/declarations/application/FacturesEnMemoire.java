package fr.bnia.declarations.application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Projection des factures en mémoire, idempotente comme l'adapter (même type, même numéro ou même pièce). */
final class FacturesEnMemoire implements FacturesDsm {

    private final List<FactureDsm> factures = new ArrayList<>();

    @Override
    public boolean inserer(FactureDsm facture) {
        if (factures.stream().anyMatch(f -> f.type() == facture.type() && (Objects.equals(f.numero(), facture.numero())
                && f.numero() != null || f.piece() == facture.piece()))) {
            return false;
        }
        return factures.add(facture);
    }

    @Override
    public void retirerBrouillon(long piece) {
        factures.removeIf(f -> f.piece() == piece && f.type() == FactureDsm.Type.BROUILLON);
    }

    @Override
    public List<FactureDsm> deLaDsm(long dsm) {
        return factures.stream().filter(f -> f.dsm() == dsm)
                .sorted(Comparator.comparing(FactureDsm::date, Comparator.nullsFirst(Comparator.<LocalDate>naturalOrder()))
                        .thenComparing(FactureDsm::type))
                .toList();
    }
}
