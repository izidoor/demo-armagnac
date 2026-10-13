package fr.bnia.commun.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VolumeAlcoolPurTest {

    @ParameterizedTest
    @CsvSource({"12, 12.00", "12.345, 12.35", "12.344, 12.34", "-0.005, -0.01", "0, 0.00"})
    void arrondi_au_centilitre_demi_superieur(String saisi, String attendu) {
        assertThat(VolumeAlcoolPur.litres(saisi).litres()).isEqualTo(new BigDecimal(attendu));
    }

    @Test
    void egalite_independante_de_l_echelle_saisie() {
        assertThat(VolumeAlcoolPur.litres("5")).isEqualTo(VolumeAlcoolPur.litres("5.00"));
    }

    @Test
    void signe_un_stock_calcule_peut_etre_negatif() {
        VolumeAlcoolPur stock = VolumeAlcoolPur.litres("10").moins(VolumeAlcoolPur.litres("12.5"));
        assertThat(stock.estNegatif()).isTrue();
        assertThat(stock).isEqualTo(VolumeAlcoolPur.litres("-2.5"));
        assertThat(stock.plus(VolumeAlcoolPur.litres("2.5")).estNul()).isTrue();
    }

    @Test
    void en_hectolitres_sans_arrondi() {
        assertThat(VolumeAlcoolPur.litres("839.11").enHectolitres()).isEqualByComparingTo("8.3911");
    }
}
