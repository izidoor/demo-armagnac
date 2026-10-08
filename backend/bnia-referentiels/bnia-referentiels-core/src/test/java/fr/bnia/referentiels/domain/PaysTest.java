package fr.bnia.referentiels.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.CompteDAge;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.referentiels.domain.partage.LangueCertificat;
import fr.bnia.referentiels.domain.partage.NiveauDetailCertificat;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PaysTest {

    @ParameterizedTest
    @ValueSource(strings = {"", "U", "USA", "U1", "É1"})
    @DisplayName("RG-REF-06 : le code ISO est l'alpha-2, deux lettres")
    void rg_ref_06_format_alpha_2(String codeIso) {
        assertThatThrownBy(() -> CodeIso.de(codeIso))
                .isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-REF-06");
    }

    @Test
    @DisplayName("RG-REF-06 : le code ISO est mis en majuscules")
    void rg_ref_06_majuscules() {
        assertThat(CodeIso.de(" us ").valeur()).isEqualTo("US");
    }

    @Test
    @DisplayName("RG-REF-07 : langue FR_EN et niveau CUMUL_DEGRE par défaut (H-REF-2)")
    void rg_ref_07_valeurs_par_defaut() {
        var description = new DescriptionPays(CodeIso.de("US"), 2L, false, null, null, null);

        assertThat(description.langueCertificat()).isEqualTo(LangueCertificat.FR_EN);
        assertThat(description.niveauDetailCertificat()).isEqualTo(NiveauDetailCertificat.CUMUL_DEGRE);
        assertThat(description.ageMinimum()).isEmpty();
    }

    @Test
    @DisplayName("RG-REF-07 : la langue et le niveau saisis sont conservés")
    void rg_ref_07_valeurs_saisies() {
        var description = new DescriptionPays(CodeIso.de("ES"), 1L, true, LangueCertificat.FR_ES,
                NiveauDetailCertificat.DETAIL_MILLESIME, Optional.empty());

        assertThat(description.langueCertificat()).isEqualTo(LangueCertificat.FR_ES);
        assertThat(description.niveauDetailCertificat()).isEqualTo(NiveauDetailCertificat.DETAIL_MILLESIME);
    }

    @Test
    @DisplayName("RG-REF-08 : l'appartenance à l'UE est indépendante de la zone commerciale")
    void rg_ref_08_ue_independante_de_la_zone() {
        var norvege = new DescriptionPays(CodeIso.de("NO"), 1L, false, null, null, null);
        var france = new DescriptionPays(CodeIso.de("FR"), 1L, true, null, null, null);

        assertThat(norvege.zoneCommerciale()).isEqualTo(france.zoneCommerciale());
        assertThat(norvege.membreUe()).isFalse();
        assertThat(france.membreUe()).isTrue();
    }

    @Test
    @DisplayName("RG-REF-09 : l'âge minimum est un compte d'âge, 00 et 0 compris")
    void rg_ref_09_age_minimum_compte_d_age() {
        assertThat(DescriptionPays.ageMinimum("00")).contains(CompteDAge.DOUBLE_ZERO);
        assertThat(DescriptionPays.ageMinimum("4")).contains(CompteDAge.de(4));
        assertThat(DescriptionPays.ageMinimum(" ")).isEmpty();
        assertThat(DescriptionPays.ageMinimum(null)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"VSOP", "-1", "4.5", "000"})
    @DisplayName("RG-REF-09 : un âge minimum qui n'est pas un compte d'âge est refusé")
    void rg_ref_09_age_minimum_invalide(String saisie) {
        assertThatThrownBy(() -> DescriptionPays.ageMinimum(saisie))
                .isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-REF-09");
    }

    @Test
    @DisplayName("RG-REF-02 : tout se corrige dans un pays, code compris")
    void rg_ref_02_decrire_corrige_tout() {
        var pays = Pays.creer(Code.de("US"), Libelle.de("USA"),
                new DescriptionPays(CodeIso.de("US"), 2L, false, null, null, null));

        pays.decrire(Code.de("USA"), Libelle.de("États-Unis"),
                new DescriptionPays(CodeIso.de("US"), 2L, false, null, null, Optional.of(CompteDAge.de(4))));

        assertThat(pays.code()).isEqualTo(Code.de("USA"));
        assertThat(pays.libelle()).isEqualTo(Libelle.de("États-Unis"));
        assertThat(pays.description().ageMinimum()).contains(CompteDAge.de(4));
    }
}
