package fr.bnia.declarations.application;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/** Retards signalés en mémoire : l'unicité de la table {@code retard_signale}. */
final class RetardsEnMemoire implements RetardsSignales {

    private final Set<String> signales = new HashSet<>();

    @Override
    public boolean signaler(long operateur, LocalDate debut, Instant le) {
        return signales.add(operateur + "@" + debut);
    }
}
