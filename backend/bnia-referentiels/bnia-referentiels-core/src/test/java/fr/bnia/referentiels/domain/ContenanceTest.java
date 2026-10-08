package fr.bnia.referentiels.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ContenanceTest {

    @ParameterizedTest
    @ValueSource(strings = {"0", "-0.7", "0.0001", "abc", ""})
    @DisplayName("RG-REF-10 : le volume est strictement positif, trois décimales au plus")
    void rg_ref_10_volume_invalide(String volume) {
        assertThatThrownBy(() -> Volume.de(volume))
                .isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-REF-10");
    }

    @Test
    @DisplayName("RG-REF-10 : 0.7 et 0.700 sont le même volume")
    void rg_ref_10_meme_volume_quelle_que_soit_l_ecriture() {
        assertThat(Volume.de(new BigDecimal("0.7"))).isEqualTo(Volume.de("0,700"));
        assertThat(Volume.de("1.5000").litres()).isEqualByComparingTo("1.5");
    }

    @ParameterizedTest
    @CsvSource({"0.05, '0,05 L'", "0.7, '0,70 L'", "0.375, '0,375 L'", "3, '3,00 L'"})
    void le_libelle_est_derive_du_volume(String volume, String libelle) {
        assertThat(Contenance.creer(Volume.de(volume), null).libelle()).isEqualTo(libelle);
    }

    @Test
    void un_libelle_saisi_remplace_le_libelle_derive() {
        var contenance = Contenance.creer(Volume.de("0.7"), Libelle.de("Bouteille 70 cl"));

        assertThat(contenance.libelle()).isEqualTo("Bouteille 70 cl");

        contenance.decrire(Volume.de("0.75"), null);
        assertThat(contenance.libelle()).isEqualTo("0,75 L");
    }

    @Test
    @DisplayName("RG-REF-04 : une contenance se désactive puis se réactive")
    void rg_ref_04_desactiver_reactiver() {
        var contenance = Contenance.creer(Volume.de("0.7"), null);

        contenance.desactiver();
        assertThat(contenance.estActif()).isFalse();
        contenance.reactiver();
        assertThat(contenance.estActif()).isTrue();
    }
}
