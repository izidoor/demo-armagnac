package fr.bnia.referentiels.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ClasseTvaTest {

    private final ClasseTva normal = ClasseTva.creer(Code.de("NORMAL"), Libelle.de("Normal"));

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({
            "2013-01-01, 2014-01-01",  // finit le jour où commence le taux existant
            "2015-01-01, 2015-06-30",  // incluse dans le taux existant
            "2019-06-01, ",            // sans fin, commence dans le taux existant
            "2010-01-01, ",            // sans fin, commence avant : couvre tout
            "2019-12-31, 2020-12-31"}) // touche la fin du taux existant
    @DisplayName("RG-REF-11 : deux taux d'une classe n'ont aucune date commune")
    void rg_ref_11_chevauchement_refuse(String debut, String fin) {
        normal.ajouterTaux(taux("20", "2014-01-01", "2019-12-31"));

        assertThatThrownBy(() -> normal.ajouterTaux(taux("10", debut, fin)))
                .isInstanceOf(RegleEnfreinte.class)
                .hasMessageContaining("chevauche le taux 20,00 % du 01/01/2014 au 31/12/2019")
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-REF-11");
    }

    @Test
    @DisplayName("RG-REF-11 : un taux sans fin couvre toutes les dates postérieures à son début")
    void rg_ref_11_taux_sans_fin() {
        normal.ajouterTaux(taux("20", "2014-01-01", null));

        assertThatThrownBy(() -> normal.ajouterTaux(taux("21", "2030-01-01", null)))
                .isInstanceOf(RegleEnfreinte.class)
                .hasMessageContaining("20,00 % à partir du 01/01/2014");
    }

    @Test
    @DisplayName("RG-REF-11 : des périodes contiguës ou séparées par un trou sont acceptées")
    void rg_ref_11_periodes_contigues_ou_avec_trou() {
        normal.ajouterTaux(taux("19.6", "2000-01-01", "2013-12-31"));
        normal.ajouterTaux(taux("20", "2014-01-01", "2019-12-31"));
        normal.ajouterTaux(taux("21", "2021-01-01", null));

        assertThat(normal.taux()).extracting(TauxTva::debut).containsExactly(
                LocalDate.parse("2021-01-01"), LocalDate.parse("2014-01-01"), LocalDate.parse("2000-01-01"));
    }

    @Test
    @DisplayName("RG-REF-11 : corriger un taux ne peut pas créer de chevauchement avec un autre")
    void rg_ref_11_correction_avec_chevauchement() {
        normal.ajouterTaux(taux("19.6", "2000-01-01", "2013-12-31"));
        normal.ajouterTaux(taux("20", "2014-01-01", null));

        assertThatThrownBy(() -> normal.modifierTaux(LocalDate.parse("2000-01-01"), taux("19.6", "2000-01-01", "2014-06-30")))
                .isInstanceOf(RegleEnfreinte.class);

        normal.modifierTaux(LocalDate.parse("2014-01-01"), taux("20", "2014-01-01", "2024-12-31"));
        assertThat(normal.taux().getFirst().fin()).isEqualTo(LocalDate.parse("2024-12-31"));
    }

    @Test
    @DisplayName("RG-REF-12 : le taux applicable est celui dont la période contient la date")
    void rg_ref_12_taux_applicable() {
        normal.ajouterTaux(taux("19.6", "2000-01-01", "2013-12-31"));
        normal.ajouterTaux(taux("20", "2014-01-01", null));

        assertThat(normal.tauxApplicable(LocalDate.parse("2013-12-31")).orElseThrow().taux()).isEqualByComparingTo("19.6");
        assertThat(normal.tauxApplicable(LocalDate.parse("2014-01-01")).orElseThrow().taux()).isEqualByComparingTo("20");
        assertThat(normal.tauxApplicable(LocalDate.parse("2099-06-30")).orElseThrow().taux()).isEqualByComparingTo("20");
    }

    @Test
    @DisplayName("RG-REF-12 : sans taux à cette date, aucun taux, jamais une valeur par défaut")
    void rg_ref_12_aucun_taux_applicable() {
        normal.ajouterTaux(taux("20", "2014-01-01", "2019-12-31"));

        assertThat(normal.tauxApplicable(LocalDate.parse("2013-12-31"))).isEmpty();
        assertThat(normal.tauxApplicable(LocalDate.parse("2020-01-01"))).isEmpty();
        assertThat(ClasseTva.creer(Code.de("VIDE"), Libelle.de("Vide")).tauxApplicable(LocalDate.now())).isEmpty();
    }

    @Test
    @DisplayName("RG-REF-13 : une classe inactive garde ses taux, consultables à une date")
    void rg_ref_13_classe_inactive_consultable() {
        normal.ajouterTaux(taux("20", "2014-01-01", null));

        normal.desactiver();

        assertThat(normal.estActif()).isFalse();
        assertThat(normal.taux()).hasSize(1);
        assertThat(normal.tauxApplicable(LocalDate.parse("2016-03-01"))).isPresent();
    }

    @Test
    void retirer_un_taux_designe_par_sa_date_de_debut() {
        normal.ajouterTaux(taux("20", "2014-01-01", null));

        assertThatThrownBy(() -> normal.retirerTaux(LocalDate.parse("2015-01-01"))).isInstanceOf(ElementIntrouvable.class);
        normal.retirerTaux(LocalDate.parse("2014-01-01"));
        assertThat(normal.taux()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"-1, 2014-01-01, ", "100.01, 2014-01-01, ", "20.555, 2014-01-01, ", "20, 2014-01-01, 2013-12-31"})
    void un_taux_invalide_est_refuse(String pourcentage, String debut, String fin) {
        assertThatThrownBy(() -> taux(pourcentage, debut, fin)).isInstanceOf(RegleEnfreinte.class);
    }

    private static TauxTva taux(String pourcentage, String debut, String fin) {
        return new TauxTva(new BigDecimal(pourcentage), LocalDate.parse(debut), fin == null ? null : LocalDate.parse(fin));
    }
}
