package fr.bnia.facturation.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.facturation.api.CategorieTva.Code;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CategorieTvaTest {

    @Test
    @DisplayName("RG-FAC-07 : S a un taux strictement positif ; Z et E un taux nul ; O aucun ; E et O un motif")
    void rg_fac_07_categories() {
        assertThat(CategorieTva.normale(new BigDecimal("20")).libelle()).isEqualTo("20,00 %");
        assertThat(CategorieTva.exoneree("Part non assujettie").tauxApplique()).isEqualByComparingTo("0");
        assertThat(new CategorieTva(Code.O, null, "Hors champ").tauxApplique()).isEqualByComparingTo("0");
        refusee(Code.S, BigDecimal.ZERO, null);
        refusee(Code.S, new BigDecimal("20"), "motif");
        refusee(Code.Z, new BigDecimal("5.5"), null);
        refusee(Code.E, BigDecimal.ZERO, null);
        refusee(Code.O, BigDecimal.ZERO, "motif");
        refusee(Code.O, null, " ");
    }

    private static void refusee(Code code, BigDecimal taux, String motif) {
        assertThatThrownBy(() -> new CategorieTva(code, taux, motif)).as(code + " " + taux + " " + motif)
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.regle()).contains("RG-FAC-07"));
    }
}
