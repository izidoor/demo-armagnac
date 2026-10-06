package fr.bnia.referentiels.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LibelleNormaliseTest {

    @Test
    @DisplayName("RG-REF-15 : la casse, les accents et les espaces superflus sont ignorés")
    void rg_ref_15_casse_accents_et_espaces_ignores() {
        assertThat(LibelleNormalise.de("Château  X")).isEqualTo(LibelleNormalise.de(" CHATEAU X "));
        assertThat(LibelleNormalise.de("Domaine de l'Œil-de-bœuf").valeur()).isEqualTo("DOMAINE DE L'ŒIL-DE-BŒUF");
        assertThat(LibelleNormalise.de("Pellehaut\tréserve").valeur()).isEqualTo("PELLEHAUT RESERVE");
    }

    @Test
    @DisplayName("RG-REF-15 : la ponctuation n'est pas ignorée")
    void rg_ref_15_ponctuation_conservee() {
        assertThat(LibelleNormalise.de("Saint-Jean")).isNotEqualTo(LibelleNormalise.de("Saint Jean"));
    }

    @Test
    void la_marque_expose_son_libelle_normalise() {
        var marque = Marque.creer(Code.de("CX"), Libelle.de("Château X"));

        assertThat(marque.libelleNormalise().valeur()).isEqualTo("CHATEAU X");
    }
}
