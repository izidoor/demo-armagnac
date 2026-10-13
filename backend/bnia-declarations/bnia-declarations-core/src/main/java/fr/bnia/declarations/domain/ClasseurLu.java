package fr.bnia.declarations.domain;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Le modèle Excel de la DSM, lu tel quel par l'adapter (ADR-020, H-DEC-15) ; {@link ImportExcel} le contrôle.
 *
 * @param cellulePeriode la cellule de la période du fichier
 * @param mois           le mois qu'elle porte, vide si elle est vide ou illisible
 * @param rangees        les rangées de lignes, dans l'ordre du classeur, rangées vierges comprises
 */
public record ClasseurLu(CelluleLue cellulePeriode, Optional<YearMonth> mois, List<Rangee> rangees) {

    public ClasseurLu {
        Objects.requireNonNull(cellulePeriode, "cellulePeriode");
        Objects.requireNonNull(mois, "mois");
        rangees = List.copyOf(rangees);
    }

    /**
     * Une ligne de DSM dans le classeur : son identité, lue au récapitulatif, et ses volumes, lus chacun dans
     * l'onglet de sa colonne, colonnes 1 et 26 comprises.
     *
     * @param ligne le numéro de ligne au récapitulatif
     */
    public record Rangee(int ligne, CelluleLue designation, CelluleLue cle, CelluleLue compte,
            Map<ColonneDsm, CelluleLue> volumes) {

        public Rangee {
            volumes = Map.copyOf(volumes);
        }

        boolean vide() {
            return designation.vide() && cle.vide() && compte.vide()
                    && volumes.values().stream().allMatch(CelluleLue::vide);
        }
    }
}
