package fr.bnia.referentiels.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.ZoneCommerciale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Cas d'usage communs aux listes simples, vérifiés sur la zone commerciale. */
class GererZonesCommercialesTest {

    private final ZonesEnMemoire zones = new ZonesEnMemoire();
    private final GererZonesCommerciales gerer = new GererZonesCommerciales(zones, zones);

    @Test
    void creer_enregistre_une_valeur_active() {
        gerer.creer(" EUR ", " Europe ");

        var zone = zones.parCode(Code.de("EUR")).orElseThrow();
        assertThat(zone.libelle().valeur()).isEqualTo("Europe");
        assertThat(zone.estActif()).isTrue();
    }

    @Test
    @DisplayName("RG-REF-01 : le code est unique, sans distinction de casse")
    void rg_ref_01_code_unique_sans_distinction_de_casse() {
        gerer.creer("EUR", "Europe");

        assertThatThrownBy(() -> gerer.creer("eur", "Europe bis"))
                .isInstanceOf(ConflitUnicite.class)
                .hasMessageContaining("eur")
                .extracting(e -> ((ConflitUnicite) e).regle().orElseThrow()).isEqualTo("RG-REF-01");
    }

    @Test
    @DisplayName("RG-REF-02 : modifier change le libellé, jamais le code")
    void rg_ref_02_modifier_ne_change_que_le_libelle() {
        gerer.creer("EUR", "Europe");

        var zone = gerer.modifier("EUR", "Europe géographique");

        assertThat(zone.code().valeur()).isEqualTo("EUR");
        assertThat(zones.parCode(Code.de("EUR")).orElseThrow().libelle().valeur()).isEqualTo("Europe géographique");
    }

    @Test
    @DisplayName("RG-REF-04 : une valeur désactivée reste lisible, et sort des valeurs actives")
    void rg_ref_04_desactivee_mais_lisible() {
        gerer.creer("EUR", "Europe");
        gerer.creer("ASI", "Asie");

        gerer.desactiver("EUR");

        assertThat(gerer.consulter("EUR").estActif()).isFalse();
        assertThat(gerer.lister(new FiltreValeurs(true, null))).extracting(ValeurSimpleLigne::code).containsExactly("ASI");
        assertThat(gerer.lister(FiltreValeurs.TOUTES)).hasSize(2);

        gerer.reactiver("EUR");
        assertThat(gerer.consulter("EUR").estActif()).isTrue();
    }

    @Test
    void lister_filtre_sur_le_code_ou_le_libelle() {
        gerer.creer("EUR", "Europe");
        gerer.creer("ASI", "Asie");

        assertThat(gerer.lister(new FiltreValeurs(null, "eur"))).extracting(ValeurSimpleLigne::code).containsExactly("EUR");
        assertThat(gerer.lister(new FiltreValeurs(null, "sie"))).extracting(ValeurSimpleLigne::code).containsExactly("ASI");
    }

    @Test
    void une_valeur_inconnue_est_introuvable() {
        assertThatThrownBy(() -> gerer.consulter("XXX")).isInstanceOf(ElementIntrouvable.class);
        assertThatThrownBy(() -> gerer.desactiver("XXX")).isInstanceOf(ElementIntrouvable.class);
    }

    private static final class ZonesEnMemoire extends ValeursEnMemoire<ZoneCommerciale>
            implements ZoneCommercialeRepository {
    }
}
