package fr.bnia.operateurs.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Formats contrôlés de la fiche : identifiants administratifs, e-mail, sites, coordonnées GPS. */
class FormatsTest {

    @ParameterizedTest
    @ValueSource(strings = {"41234567800012", "412 345 678 00012", "73282932000074"})
    @DisplayName("RG-OPE-06 : SIRET sur 14 chiffres avec clé de Luhn, espaces ignorés")
    void rg_ope_06_siret_valide(String saisie) {
        assertThat(Siret.facultatif(saisie).valeur()).hasSize(14).doesNotContain(" ");
    }

    @ParameterizedTest
    @ValueSource(strings = {"41234567800017", "4123456780001", "4123456780001A"})
    @DisplayName("RG-OPE-06 : SIRET mal formé ou à la clé fausse, refusé")
    void rg_ope_06_siret_invalide(String saisie) {
        assertThatThrownBy(() -> Siret.facultatif(saisie)).isInstanceOf(RegleEnfreinte.class).hasMessageContaining("SIRET");
    }

    @Test
    @DisplayName("RG-OPE-06 : CVI sur 10 caractères alphanumériques, accise FR et 11 caractères ; facultatifs")
    void rg_ope_06_cvi_et_accise() {
        assertThat(Cvi.facultatif("32032 00123").valeur()).isEqualTo("3203200123");
        assertThat(NumeroAccise.facultatif("fr012345e6789").valeur()).isEqualTo("FR012345E6789");
        assertThat(Cvi.facultatif(" ")).isNull();
        assertThat(NumeroAccise.facultatif(null)).isNull();
        assertThatThrownBy(() -> Cvi.facultatif("320320012")).isInstanceOf(RegleEnfreinte.class);
        assertThatThrownBy(() -> NumeroAccise.facultatif("BE012345E6789")).isInstanceOf(RegleEnfreinte.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"contact", "contact@", "@exemple.fr", "contact@exemple", "a b@exemple.fr"})
    @DisplayName("RG-OPE-24 : l'e-mail général respecte le format d'une adresse e-mail")
    void rg_ope_24_email_invalide(String saisie) {
        assertThatThrownBy(() -> Email.facultatif(saisie, "RG-OPE-24"))
                .isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-24");
    }

    @Test
    @DisplayName("RG-OPE-24 : e-mail comparé sans distinction de casse")
    void rg_ope_24_email_valide() {
        assertThat(Email.de(" Jeanne.Durand@Exemple.fr ", "RG-OPE-24").cle()).isEqualTo("jeanne.durand@exemple.fr");
    }

    @Test
    @DisplayName("RG-OPE-20 : les sites sont des URL http ou https")
    void rg_ope_20_sites() {
        assertThat(SiteWeb.facultatif("https://www.domaine-durand.fr").valeur()).isEqualTo("https://www.domaine-durand.fr");
        assertThat(SiteWeb.facultatif("")).isNull();
        assertThatThrownBy(() -> SiteWeb.facultatif("www.domaine-durand.fr")).isInstanceOf(RegleEnfreinte.class);
        assertThatThrownBy(() -> SiteWeb.facultatif("ftp://domaine-durand.fr")).isInstanceOf(RegleEnfreinte.class);
    }

    @ParameterizedTest
    @CsvSource(value = {"43.958, NULL", "NULL, 0.372", "90.1, 0", "0, -180.5"}, nullValues = "NULL")
    @DisplayName("RG-OPE-20 : latitude et longitude ensemble, dans leurs bornes")
    void rg_ope_20_gps_invalide(BigDecimal latitude, BigDecimal longitude) {
        assertThatThrownBy(() -> CoordonneesGps.facultatives(latitude, longitude)).isInstanceOf(RegleEnfreinte.class);
    }

    @Test
    @DisplayName("RG-OPE-20 : coordonnées GPS facultatives")
    void rg_ope_20_gps_valide() {
        assertThat(CoordonneesGps.facultatives(null, null)).isNull();
        assertThat(CoordonneesGps.facultatives(new BigDecimal("-90"), new BigDecimal("180"))).isNotNull();
    }
}
