package fr.bnia.referentiels.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.RegleEnfreinte;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Règles communes aux listes simples, vérifiées sur la zone commerciale. */
class ZoneCommercialeTest {

    @Test
    void une_valeur_creee_est_active() {
        var zone = ZoneCommerciale.creer(Code.de("EUR"), Libelle.de("Europe"));

        assertThat(zone.estActif()).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    @DisplayName("RG-REF-01 : le code est obligatoire")
    void rg_ref_01_code_obligatoire(String code) {
        assertThatThrownBy(() -> Code.de(code))
                .isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-REF-01");
    }

    @Test
    @DisplayName("RG-REF-01 : les espaces en tête et en fin du code sont ignorés")
    void rg_ref_01_code_sans_espaces_en_tete_et_en_fin() {
        assertThat(Code.de("  EUR ")).isEqualTo(Code.de("EUR"));
    }

    @Test
    @DisplayName("RG-REF-02 : renommer ne change pas le code")
    void rg_ref_02_code_immuable() {
        var zone = ZoneCommerciale.creer(Code.de("EUR"), Libelle.de("Europe"));

        zone.renommer(Libelle.de("Europe géographique"));

        assertThat(zone.code()).isEqualTo(Code.de("EUR"));
        assertThat(zone.libelle()).isEqualTo(Libelle.de("Europe géographique"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    @DisplayName("RG-REF-03 : le libellé est obligatoire")
    void rg_ref_03_libelle_obligatoire(String libelle) {
        assertThatThrownBy(() -> Libelle.de(libelle))
                .isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-REF-03");
    }

    @Test
    @DisplayName("RG-REF-04 : une valeur se désactive puis se réactive")
    void rg_ref_04_desactiver_puis_reactiver() {
        var zone = ZoneCommerciale.creer(Code.de("EUR"), Libelle.de("Europe"));

        zone.desactiver();
        assertThat(zone.estActif()).isFalse();

        zone.reactiver();
        assertThat(zone.estActif()).isTrue();
    }
}
