package fr.bnia.contratsarmagnac.domain;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import fr.bnia.declarations.domain.partage.CompteDAgeDsm;
import fr.bnia.declarations.domain.partage.StockEstimeVue.LigneStockEstimeVue;
import fr.bnia.declarations.domain.partage.StockEstimeVue;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Le stock estimé de la Maison Durand, Condom, comme en démonstration (spec 06 §7.1). */
public final class StocksExemples {

    private StocksExemples() {
    }

    public static StockEstimeVue durand() {
        return new StockEstimeVue(8, Optional.of(LocalDate.of(2026, 3, 31)), List.of(
                ligne(CleConditionnement.MB, 2015, null, "10", "816.80"),
                ligne(CleConditionnement.NB, null, "Hors d'âge", "20+", "750"),
                ligne(CleConditionnement.MV, 2012, null, "10", "120")));
    }

    private static LigneStockEstimeVue ligne(CleConditionnement cle, Integer millesime, String denomination,
            String compte, String litres) {
        VolumeAlcoolPur volume = VolumeAlcoolPur.litres(litres);
        return new LigneStockEstimeVue(cle, millesime, denomination, CompteDAgeDsm.de(compte), volume,
                VolumeAlcoolPur.ZERO, VolumeAlcoolPur.ZERO, volume);
    }
}
