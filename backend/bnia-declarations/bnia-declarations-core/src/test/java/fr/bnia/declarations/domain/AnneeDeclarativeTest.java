package fr.bnia.declarations.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AnneeDeclarativeTest {

    @Test
    @DisplayName("RG-DEC-44 : l'année déclarative va du 1er avril au 31 mars")
    void rg_dec_44_bornes() {
        AnneeDeclarative annee = AnneeDeclarative.contenant(LocalDate.of(2027, 3, 31));
        assertThat(annee.debutAnnee()).isEqualTo(2026);
        assertThat(annee.debut()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(annee.fin()).isEqualTo(LocalDate.of(2027, 3, 31));
        assertThat(annee.libelle()).isEqualTo("2026-2027");
        assertThat(AnneeDeclarative.contenant(LocalDate.of(2026, 4, 1))).isEqualTo(annee);

        assertThat(annee.contient(PeriodeDeclaration.commencant(TypeDeclarant.PRODUCTEUR, LocalDate.of(2026, 4, 1))))
                .isTrue();
        assertThat(annee.contient(PeriodeDeclaration.commencant(TypeDeclarant.NEGOCIANT, LocalDate.of(2027, 3, 1))))
                .isTrue();
        assertThat(annee.contient(PeriodeDeclaration.commencant(TypeDeclarant.NEGOCIANT, LocalDate.of(2026, 3, 1))))
                .isFalse();
    }

    @Test
    @DisplayName("RG-DEC-44 : les fins de mois écoulées de l'année, sans celle du mois en cours")
    void rg_dec_44_fins_de_mois() {
        AnneeDeclarative annee = new AnneeDeclarative(2026);
        assertThat(annee.finsDeMois(LocalDate.of(2026, 10, 13))).containsExactly(LocalDate.of(2026, 4, 30),
                LocalDate.of(2026, 5, 31), LocalDate.of(2026, 6, 30), LocalDate.of(2026, 7, 31),
                LocalDate.of(2026, 8, 31), LocalDate.of(2026, 9, 30));
        assertThat(annee.finsDeMois(LocalDate.of(2026, 10, 1))).hasSize(6).last()
                .isEqualTo(LocalDate.of(2026, 9, 30));
        assertThat(annee.finsDeMois(LocalDate.of(2030, 1, 1))).hasSize(12).last()
                .isEqualTo(LocalDate.of(2027, 3, 31));
        assertThat(annee.finsDeMois(LocalDate.of(2026, 4, 30))).isEmpty();
    }
}
