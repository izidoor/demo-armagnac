package fr.bnia.referentiels.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.CodeIso;
import fr.bnia.referentiels.domain.Pays;
import fr.bnia.referentiels.domain.ZoneCommerciale;
import fr.bnia.referentiels.domain.partage.LangueCertificat;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GererPaysTest {

    private final ZonesEnMemoire zones = new ZonesEnMemoire();
    private final PaysEnMemoire pays = new PaysEnMemoire();
    private final GererPays gerer = new GererPays(pays, pays, zones);

    @BeforeEach
    void zones() {
        var gererZones = new GererZonesCommerciales(zones, zones);
        gererZones.creer("EUR", "Europe");
        gererZones.creer("AMN", "Amérique du Nord");
        gererZones.creer("OLD", "Ancienne zone");
        gererZones.desactiver("OLD");
    }

    @Test
    void creer_un_pays_avec_ses_valeurs_par_defaut() {
        var cree = gerer.creer("US", saisie("US", "AMN"));

        assertThat(cree.estActif()).isTrue();
        assertThat(cree.description().langueCertificat()).isEqualTo(LangueCertificat.FR_EN);
    }

    @Test
    @DisplayName("RG-REF-01 : le code d'un pays est unique")
    void rg_ref_01_code_unique() {
        gerer.creer("US", saisie("US", "AMN"));

        assertThatThrownBy(() -> gerer.creer("us", saisie("UM", "AMN")))
                .isInstanceOf(ConflitUnicite.class)
                .extracting(e -> ((ConflitUnicite) e).regle().orElseThrow()).isEqualTo("RG-REF-01");
    }

    @Test
    @DisplayName("RG-REF-05 : la zone commerciale doit être active à la création")
    void rg_ref_05_zone_inactive_refusee() {
        assertThatThrownBy(() -> gerer.creer("XX", saisie("XX", "OLD")))
                .isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-REF-05");
        assertThatThrownBy(() -> gerer.creer("XX", saisie("XX", "INCONNUE")))
                .isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-REF-05");
        assertThatThrownBy(() -> gerer.creer("XX", saisie("XX", " ")))
                .isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-REF-05");
    }

    @Test
    @DisplayName("RG-REF-05 : désactiver une zone ne touche pas ses pays, qui restent modifiables")
    void rg_ref_05_desactiver_une_zone_ne_touche_pas_ses_pays() {
        gerer.creer("FR", saisie("FR", "EUR"));
        new GererZonesCommerciales(zones, zones).desactiver("EUR");

        var modifie = gerer.modifier("FR", new SaisiePays("France métropolitaine", "FR", "EUR", true, null, null, null));

        assertThat(modifie.description().zoneCommerciale()).isEqualTo(Code.de("EUR"));
        assertThat(modifie.estActif()).isTrue();
    }

    @Test
    @DisplayName("RG-REF-05 : changer de zone exige une zone active")
    void rg_ref_05_changer_vers_une_zone_inactive() {
        gerer.creer("FR", saisie("FR", "EUR"));

        assertThatThrownBy(() -> gerer.modifier("FR", saisie("FR", "OLD"))).isInstanceOf(RegleEnfreinte.class);
    }

    @Test
    @DisplayName("RG-REF-06 : le code ISO est unique, sauf pour le pays lui-même")
    void rg_ref_06_code_iso_unique() {
        gerer.creer("US", saisie("US", "AMN"));

        assertThatThrownBy(() -> gerer.creer("USA", saisie("us", "AMN")))
                .isInstanceOf(ConflitUnicite.class)
                .extracting(e -> ((ConflitUnicite) e).regle().orElseThrow()).isEqualTo("RG-REF-06");
        assertThat(gerer.modifier("US", saisie("US", "AMN")).code()).isEqualTo(Code.de("US"));
    }

    private static SaisiePays saisie(String codeIso, String zone) {
        return new SaisiePays("Pays " + codeIso, codeIso, zone, false, null, null, null);
    }

    private static final class ZonesEnMemoire extends ValeursSimplesEnMemoire<ZoneCommerciale>
            implements ZoneCommercialeRepository {
    }

    private static final class PaysEnMemoire extends ValeursEnMemoire<Pays> implements PaysRepository, PaysLecture {

        @Override
        public boolean existeCodeIso(CodeIso codeIso, Code sauf) {
            return valeurs.values().stream()
                    .anyMatch(p -> p.description().codeIso().equals(codeIso) && !p.code().equals(sauf));
        }

        @Override
        public List<PaysLigne> lister(FiltreValeurs filtre) {
            throw new UnsupportedOperationException();
        }
    }
}
