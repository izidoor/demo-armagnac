package fr.bnia.facturation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.RegleEnfreinte;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NumerotationTest {

    @Test
    @DisplayName("RG-FAC-12 : l'année suivie de 6 chiffres, sans trou")
    void rg_fac_12_numero() {
        Numerotation premiere = new Numerotation(2026, 0).suivante();

        assertThat(premiere.numero()).isEqualTo("2026000001");
        assertThat(premiere.suivante().numero()).isEqualTo("2026000002");
        assertThatThrownBy(() -> new Numerotation(2026, 999_999).suivante())
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.regle()).contains("RG-FAC-12"));
    }
}
