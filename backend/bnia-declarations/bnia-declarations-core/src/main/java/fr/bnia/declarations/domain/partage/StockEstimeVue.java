package fr.bnia.declarations.domain.partage;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Le stock estimé d'un opérateur (RG-CA-11, spec 06 §3.7), lu par {@code contratsarmagnac} : par ligne de DSM, la
 * base (dernière DSM validée, sinon point de départ, vieillie à aujourd'hui), plus les achats, moins les ventes de
 * ses mouvements de contrat postérieurs. Sans base, aucune ligne.
 *
 * @param baseAu date de la base : fin de la DSM validée, ou début du point de départ ; vide sans base
 */
public record StockEstimeVue(long operateur, Optional<LocalDate> baseAu, List<LigneStockEstimeVue> lignes) {

    public StockEstimeVue {
        lignes = List.copyOf(lignes);
    }

    /**
     * Une ligne : sa clé, son millésime (lignes {@code M}) ou sa dénomination (lignes {@code N}), son compte d'âge,
     * en litres d'alcool pur.
     */
    public record LigneStockEstimeVue(CleConditionnement cle, Integer millesime, String denomination,
            CompteDAgeDsm compte, VolumeAlcoolPur base, VolumeAlcoolPur achats, VolumeAlcoolPur ventes,
            VolumeAlcoolPur estime) {
    }
}
