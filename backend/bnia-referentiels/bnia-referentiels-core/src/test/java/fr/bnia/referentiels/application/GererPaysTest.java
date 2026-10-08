package fr.bnia.referentiels.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.referentiels.application.GererZonesCommercialesTest.ZonesEnMemoire;
import fr.bnia.referentiels.domain.CodeIso;
import fr.bnia.referentiels.domain.Pays;
import fr.bnia.referentiels.domain.partage.LangueCertificat;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GererPaysTest {

    private final ZonesEnMemoire zones = new ZonesEnMemoire();
    private final PaysEnMemoire pays = new PaysEnMemoire();
    private final GererPays gerer = new GererPays(pays, pays, zones);
    private final GererZonesCommerciales gererZones = new GererZonesCommerciales(zones, zones);
    private long europe;
    private long ameriqueNord;
    private long ancienne;

    @BeforeEach
    void zones() {
        europe = gererZones.creer("EUR", "Europe").id();
        ameriqueNord = gererZones.creer("AMN", "Amérique du Nord").id();
        ancienne = gererZones.creer("OLD", "Ancienne zone").id();
        gererZones.desactiver(ancienne);
    }

    @Test
    void creer_un_pays_avec_ses_valeurs_par_defaut() {
        var cree = gerer.creer(saisie("US", "US", ameriqueNord));

        assertThat(cree.id()).isNotNull();
        assertThat(cree.estActif()).isTrue();
        assertThat(cree.description().langueCertificat()).isEqualTo(LangueCertificat.FR_EN);
    }

    @Test
    @DisplayName("RG-REF-01 : le code d'un pays est unique")
    void rg_ref_01_code_unique() {
        gerer.creer(saisie("US", "US", ameriqueNord));

        assertThatThrownBy(() -> gerer.creer(saisie("us", "UM", ameriqueNord)))
                .isInstanceOf(ConflitUnicite.class)
                .extracting(e -> ((ConflitUnicite) e).regle().orElseThrow()).isEqualTo("RG-REF-01");
    }

    @Test
    @DisplayName("RG-REF-02 : le code d'un pays se corrige")
    void rg_ref_02_code_corrige() {
        long id = gerer.creer(saisie("USA", "US", ameriqueNord)).id();

        assertThat(gerer.modifier(id, saisie("US", "US", ameriqueNord)).code().valeur()).isEqualTo("US");
    }

    @Test
    @DisplayName("RG-REF-05 : la zone commerciale doit exister et être active à la création")
    void rg_ref_05_zone_inactive_refusee() {
        assertThatThrownBy(() -> gerer.creer(saisie("XX", "XX", ancienne)))
                .isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-REF-05");
        assertThatThrownBy(() -> gerer.creer(saisie("XX", "XX", 999L)))
                .isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-REF-05");
        assertThatThrownBy(() -> gerer.creer(saisie("XX", "XX", null)))
                .isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-REF-05");
    }

    @Test
    @DisplayName("RG-REF-05 : désactiver une zone ne touche pas ses pays, qui restent modifiables")
    void rg_ref_05_desactiver_une_zone_ne_touche_pas_ses_pays() {
        long france = gerer.creer(saisie("FR", "FR", europe)).id();
        gererZones.desactiver(europe);

        var modifie = gerer.modifier(france,
                new SaisiePays("FR", "France métropolitaine", "FR", europe, true, null, null, null));

        assertThat(modifie.description().zoneCommerciale()).isEqualTo(europe);
        assertThat(modifie.estActif()).isTrue();
    }

    @Test
    @DisplayName("RG-REF-05 : changer de zone exige une zone active")
    void rg_ref_05_changer_vers_une_zone_inactive() {
        long france = gerer.creer(saisie("FR", "FR", europe)).id();

        assertThatThrownBy(() -> gerer.modifier(france, saisie("FR", "FR", ancienne))).isInstanceOf(RegleEnfreinte.class);
    }

    @Test
    @DisplayName("RG-REF-06 : le code ISO est unique, sauf pour le pays lui-même")
    void rg_ref_06_code_iso_unique() {
        long us = gerer.creer(saisie("US", "US", ameriqueNord)).id();

        assertThatThrownBy(() -> gerer.creer(saisie("USA", "us", ameriqueNord)))
                .isInstanceOf(ConflitUnicite.class)
                .extracting(e -> ((ConflitUnicite) e).regle().orElseThrow()).isEqualTo("RG-REF-06");
        assertThat(gerer.modifier(us, saisie("US", "US", ameriqueNord)).id()).isEqualTo(us);
    }

    private static SaisiePays saisie(String code, String codeIso, Long zone) {
        return new SaisiePays(code, "Pays " + code, codeIso, zone, false, null, null, null);
    }

    private static final class PaysEnMemoire extends ValeursEnMemoire<Pays> implements PaysRepository, PaysLecture {

        PaysEnMemoire() {
            super((id, p) -> Pays.reconstituer(id, p.code(), p.libelle(), p.description(), p.estActif()));
        }

        @Override
        public boolean existeCodeIso(CodeIso codeIso, Long sauf) {
            return valeurs.values().stream()
                    .anyMatch(p -> p.description().codeIso().equals(codeIso) && !p.id().equals(sauf));
        }

        @Override
        public List<PaysLigne> lister(FiltreValeurs filtre) {
            throw new UnsupportedOperationException();
        }
    }
}
