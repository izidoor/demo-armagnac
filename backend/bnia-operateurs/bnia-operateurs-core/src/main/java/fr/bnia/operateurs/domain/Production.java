package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.Appellation;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Production d'un opérateur (C.1.1.2, C.1.2 p.102) : région, appellations, alambics.
 *
 * @param region      identifiant de la région dans {@code referentiels}, ou {@code null} (RG-OPE-02, 21)
 * @param appellations choix multiple parmi les cinq AOC (RG-OPE-12)
 */
public record Production(Long region, Set<Appellation> appellations, List<Alambic> alambics) {

    public Production {
        appellations = appellations == null || appellations.isEmpty()
                ? Set.of()
                : Set.copyOf(EnumSet.copyOf(appellations));
        alambics = alambics == null ? List.of() : List.copyOf(alambics);
    }

    public static Production aucune() {
        return new Production(null, Set.of(), List.of());
    }

    /** RG-OPE-13 : détenteur d'alambic si et seulement s'il en a au moins un ; jamais saisi. */
    public boolean detenteurAlambic() {
        return !alambics.isEmpty();
    }
}
