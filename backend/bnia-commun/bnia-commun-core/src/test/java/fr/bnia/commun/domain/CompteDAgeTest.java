package fr.bnia.commun.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CompteDAgeTest {

    @Test
    void double_zero_et_zero_sont_deux_valeurs_distinctes() {
        assertThat(CompteDAge.de("00")).isNotEqualTo(CompteDAge.de("0"));
        assertThat(CompteDAge.de("00")).hasToString("00");
        assertThat(CompteDAge.de("0")).hasToString("0");
    }

    @Test
    void ordre_double_zero_puis_zero_puis_les_entiers() {
        List<CompteDAge> tries = Stream.of("10", "1", "0", "2", "00").map(CompteDAge::de).sorted().toList();

        assertThat(tries).extracting(CompteDAge::toString).containsExactly("00", "0", "1", "2", "10");
        assertThat(CompteDAge.de("00").estPlusJeuneQue(CompteDAge.de("0"))).isTrue();
        assertThat(CompteDAge.de("4").estPlusJeuneQue(CompteDAge.de("4"))).isFalse();
    }

    @Test
    void la_forme_texte_est_canonique() {
        assertThat(CompteDAge.de(" 12 ")).isEqualTo(CompteDAge.de(12)).hasToString("12");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "000", "01", "-1", "1.5", "VSOP", "20+"})
    void refuse_ce_qui_n_est_pas_un_compte_d_age(String valeur) {
        assertThatThrownBy(() -> CompteDAge.de(valeur)).isInstanceOf(RegleEnfreinte.class);
    }
}
