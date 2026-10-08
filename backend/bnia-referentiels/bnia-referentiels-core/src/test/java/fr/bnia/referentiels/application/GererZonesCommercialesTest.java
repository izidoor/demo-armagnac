package fr.bnia.referentiels.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.referentiels.domain.ZoneCommerciale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Cas d'usage communs aux listes simples, vérifiés sur la zone commerciale. */
class GererZonesCommercialesTest {

    private final ZonesEnMemoire zones = new ZonesEnMemoire();
    private final GererZonesCommerciales gerer = new GererZonesCommerciales(zones, zones);

    @Test
    void creer_enregistre_une_valeur_active_avec_un_identifiant() {
        var zone = gerer.creer(" EUR ", " Europe ");

        assertThat(zone.id()).isNotNull();
        assertThat(zone.code().valeur()).isEqualTo("EUR");
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
    @DisplayName("RG-REF-02 : le code se corrige, l'identifiant ne change pas")
    void rg_ref_02_le_code_se_corrige() {
        long id = gerer.creer("EUROPE", "Europe").id();

        var zone = gerer.modifier(id, "EUR", "Europe géographique");

        assertThat(zone.id()).isEqualTo(id);
        assertThat(zones.parId(id).orElseThrow().code().valeur()).isEqualTo("EUR");
        assertThat(zones.parId(id).orElseThrow().libelle().valeur()).isEqualTo("Europe géographique");
    }

    @Test
    @DisplayName("RG-REF-01 : une correction de code ne peut pas reprendre le code d'une autre valeur")
    void rg_ref_01_correction_vers_un_code_pris() {
        gerer.creer("EUR", "Europe");
        long asie = gerer.creer("ASI", "Asie").id();

        assertThatThrownBy(() -> gerer.modifier(asie, "eur", "Asie"))
                .isInstanceOf(ConflitUnicite.class);
        assertThat(gerer.modifier(asie, "asi", "Asie").code().valeur()).isEqualTo("asi");
    }

    @Test
    @DisplayName("RG-REF-04 : une valeur désactivée reste lisible, et sort des valeurs actives")
    void rg_ref_04_desactivee_mais_lisible() {
        long europe = gerer.creer("EUR", "Europe").id();
        gerer.creer("ASI", "Asie");

        gerer.desactiver(europe);

        assertThat(gerer.consulter(europe).estActif()).isFalse();
        assertThat(gerer.lister(new FiltreValeurs(true, null))).extracting(ValeurSimpleLigne::code).containsExactly("ASI");
        assertThat(gerer.lister(FiltreValeurs.TOUTES)).hasSize(2);

        gerer.reactiver(europe);
        assertThat(gerer.consulter(europe).estActif()).isTrue();
    }

    @Test
    @DisplayName("RG-REF-04 : une valeur que rien n'utilise se supprime")
    void rg_ref_04_supprimer() {
        long id = gerer.creer("EUR", "Europe").id();

        gerer.supprimer(id);

        assertThatThrownBy(() -> gerer.consulter(id)).isInstanceOf(ElementIntrouvable.class);
        assertThat(gerer.creer("EUR", "Europe").code().valeur()).isEqualTo("EUR");
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
        assertThatThrownBy(() -> gerer.consulter(999)).isInstanceOf(ElementIntrouvable.class);
        assertThatThrownBy(() -> gerer.desactiver(999)).isInstanceOf(ElementIntrouvable.class);
        assertThatThrownBy(() -> gerer.supprimer(999)).isInstanceOf(ElementIntrouvable.class);
    }

    static final class ZonesEnMemoire extends ValeursSimplesEnMemoire<ZoneCommerciale>
            implements ZoneCommercialeRepository {

        ZonesEnMemoire() {
            super((id, z) -> ZoneCommerciale.reconstituer(id, z.code(), z.libelle(), z.estActif()));
        }
    }
}
