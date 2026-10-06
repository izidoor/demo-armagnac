package fr.bnia.referentiels.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RegionTest {

    @Test
    void une_region_porte_l_indicateur_de_region_delimitee() {
        var gers = Region.creer(Code.de("GERS"), Libelle.de("Gers"), true);

        gers.decrire(Libelle.de("Gers (32)"), false);

        assertThat(gers.code()).isEqualTo(Code.de("GERS"));
        assertThat(gers.libelle()).isEqualTo(Libelle.de("Gers (32)"));
        assertThat(gers.estEnRegionDelimitee()).isFalse();
        assertThat(gers.estActif()).isTrue();
    }
}
