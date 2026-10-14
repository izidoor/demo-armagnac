package fr.bnia.declarations.domain.partage;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import java.time.LocalDate;
import java.util.List;

/**
 * Le stock déclaré d'un opérateur (RG-DEC-42, spec 05 §5.2) : le stock de fin (colonne 26) de sa dernière DSM
 * validée, ligne par ligne, daté de la fin de sa période. Les lignes finies à 0 n'y sont pas.
 *
 * @param dsm identifiant de la DSM dont il est tiré
 * @param au  fin de la période de cette DSM
 */
public record StockDeclareVue(long operateur, long dsm, LocalDate au, List<LigneStockVue> lignes) {

    public StockDeclareVue {
        lignes = List.copyOf(lignes);
    }

    /**
     * Une ligne : sa clé, son millésime (lignes {@code M}) ou sa dénomination (lignes {@code N}), son compte d'âge
     * et son stock, en litres d'alcool pur.
     */
    public record LigneStockVue(CleConditionnement cle, Integer millesime, String denomination, CompteDAgeDsm compte,
            VolumeAlcoolPur stock) {
    }
}
