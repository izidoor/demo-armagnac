package fr.bnia.commun.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MillesimeTest {

    @ParameterizedTest
    @ValueSource(ints = {1800, 1893, 2025, 2100})
    void accepte_une_annee_de_1800_a_2100(int annee) {
        assertThat(Millesime.de(annee).annee()).isEqualTo(annee);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 25, 1799, 2101})
    void refuse_une_annee_hors_bornes(int annee) {
        assertThatThrownBy(() -> Millesime.de(annee))
                .isInstanceOf(RegleEnfreinte.class)
                .hasMessageContaining(Integer.toString(annee));
    }

    @Test
    void ordre_chronologique_et_egalite_par_l_annee() {
        assertThat(Stream.of(2020, 1998, 2005).map(Millesime::de).sorted())
                .extracting(Millesime::toString).containsExactly("1998", "2005", "2020");
        assertThat(Millesime.de(2005)).isEqualTo(Millesime.de(2005));
    }
}
