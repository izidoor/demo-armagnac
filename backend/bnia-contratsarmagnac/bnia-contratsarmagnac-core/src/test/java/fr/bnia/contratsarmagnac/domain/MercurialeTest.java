package fr.bnia.contratsarmagnac.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.contratsarmagnac.domain.Mercuriale.LigneConclue;
import fr.bnia.declarations.domain.partage.CompteDAgeDsm;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MercurialeTest {

    private static LigneConclue ligne(long contrat, Integer millesime, String denomination, String compte,
            String litresAp, String prix) {
        return new LigneConclue(contrat, millesime, denomination, CompteDAgeDsm.de(compte),
                VolumeAlcoolPur.litres(litresAp), new BigDecimal(prix));
    }

    @Test
    @DisplayName("RG-CA-26 : par millésime, ou dénomination et compte d'âge : contrats, volume AP, prix minimum, moyen pondéré et maximum")
    void rg_ca_26_mercuriales() {
        List<Mercuriale> mercuriales = Mercuriale.calculer(List.of(
                ligne(1, 2015, null, "10", "1410", "1750"),
                ligne(2, 2015, null, "10", "705", "1600"),
                ligne(2, 2018, null, "7", "1446", "1300"),
                ligne(3, null, "Hors d'âge", "20+", "135", "2400"),
                ligne(4, null, "HORS D'AGE", "20+", "270", "2200"),
                ligne(4, null, "XO", "10", "200", "1450")));

        assertThat(mercuriales).extracting(Mercuriale::libelle, Mercuriale::contrats, m -> m.volumeAp().litres(),
                Mercuriale::prixMinimum, Mercuriale::prixMoyen, Mercuriale::prixMaximum).containsExactly(
                tuple("Millésime 2018", 1, new BigDecimal("1446.00"), new BigDecimal("1300"), new BigDecimal("1300.00"),
                        new BigDecimal("1300")),
                tuple("Millésime 2015", 2, new BigDecimal("2115.00"), new BigDecimal("1600"), new BigDecimal("1700.00"),
                        new BigDecimal("1750")),
                tuple("Hors d'âge, compte 20+", 2, new BigDecimal("405.00"), new BigDecimal("2200"),
                        new BigDecimal("2266.67"), new BigDecimal("2400")),
                tuple("XO, compte 10", 1, new BigDecimal("200.00"), new BigDecimal("1450"), new BigDecimal("1450.00"),
                        new BigDecimal("1450")));
        assertThat(Mercuriale.calculer(List.of())).isEmpty();
    }
}
