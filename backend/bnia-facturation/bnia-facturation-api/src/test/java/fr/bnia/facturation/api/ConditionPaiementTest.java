package fr.bnia.facturation.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ConditionPaiementTest {

    @ParameterizedTest
    @CsvSource({"2026-10-07, 2026-11-30", "2026-03-31, 2026-04-30", "2026-01-31, 2026-03-31"})
    @DisplayName("RG-FAC-13 : 30 jours fin de mois retrouve les échéances des factures d'exemple")
    void rg_fac_13_trente_jours_fin_de_mois(LocalDate emission, LocalDate echeance) {
        assertThat(ConditionPaiement.TRENTE_JOURS_FIN_DE_MOIS.echeance(emission)).isEqualTo(echeance);
        assertThat(ConditionPaiement.TRENTE_JOURS_FIN_DE_MOIS.libelle()).isEqualTo("30 jours fin de mois");
    }

    @Test
    @DisplayName("RG-FAC-13 : à réception, ou un nombre de jours sans fin de mois")
    void rg_fac_13_sans_fin_de_mois() {
        assertThat(new ConditionPaiement(0, false).echeance(LocalDate.of(2026, 10, 7))).isEqualTo("2026-10-07");
        assertThat(new ConditionPaiement(0, false).libelle()).isEqualTo("À réception");
        assertThat(new ConditionPaiement(60, false).echeance(LocalDate.of(2026, 10, 7))).isEqualTo("2026-12-06");
    }

    @ParameterizedTest
    @CsvSource({"-1, false", "61, false", "46, true"})
    @DisplayName("RG-FAC-13 : de 0 à 60 jours, 45 au plus avec fin de mois")
    void rg_fac_13_bornes(int jours, boolean finDeMois) {
        assertThatThrownBy(() -> new ConditionPaiement(jours, finDeMois))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.regle()).contains("RG-FAC-13"));
    }
}
