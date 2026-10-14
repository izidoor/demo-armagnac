package fr.bnia.declarations.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.Millesime;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.declarations.domain.partage.CompteDAgeDsm;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class CompteDAgeDsmTest {

    @ParameterizedTest(name = "millésime 2025 au {0} : {1}")
    @CsvSource({"2025-09-01, 00", "2026-03-31, 00", "2026-04-01, 0", "2027-03-31, 0", "2027-04-01, 1",
            "2030-04-01, 4", "2036-04-01, 10"})
    @DisplayName("RG-DEC-12 : compte calculé selon l'annexe B (exemple du millésime 2025, p.98-99)")
    void rg_dec_12_calcul_annexe_b(LocalDate date, String attendu) {
        assertThat(CompteDAgeDsm.calcule(Millesime.de(2025), date)).hasToString(attendu);
    }

    @Test
    @DisplayName("RG-DEC-12 : borné à 20+ ; un nombre de 20 ou plus vaut 20+")
    void rg_dec_12_borne_20_plus() {
        assertThat(CompteDAgeDsm.calcule(Millesime.de(1990), LocalDate.of(2026, 6, 1))).isEqualTo(CompteDAgeDsm.VINGT_ET_PLUS);
        assertThat(CompteDAgeDsm.de("25")).hasToString("20+");
        assertThat(CompteDAgeDsm.de("20+")).isEqualTo(CompteDAgeDsm.de(20));
        assertThat(CompteDAgeDsm.de("19")).hasToString("19");
    }

    @Test
    @DisplayName("RG-DEC-12 : ordre 00 < 0 < 1 < … < 20+")
    void rg_dec_12_ordre() {
        assertThat(Stream.of("20+", "1", "00", "0", "19").map(CompteDAgeDsm::de).sorted())
                .extracting(CompteDAgeDsm::toString).containsExactly("00", "0", "1", "19", "20+");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "abc", "-1", "01", "21+"})
    @DisplayName("RG-DEC-12 : un compte invalide est refusé")
    void rg_dec_12_invalide(String valeur) {
        assertThatThrownBy(() -> CompteDAgeDsm.de(valeur)).isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-DEC-12");
    }
}
