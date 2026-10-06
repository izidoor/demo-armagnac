package fr.bnia.referentiels.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.referentiels.domain.Contenance;
import fr.bnia.referentiels.domain.Volume;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GererContenancesTest {

    private final ContenancesEnMemoire contenances = new ContenancesEnMemoire();
    private final GererContenances gerer = new GererContenances(contenances, contenances);

    @Test
    @DisplayName("RG-REF-10 : le volume est unique, quelle que soit son écriture")
    void rg_ref_10_volume_unique() {
        gerer.creer(new BigDecimal("0.7"), null);

        assertThatThrownBy(() -> gerer.creer(new BigDecimal("0.700"), "Autre"))
                .isInstanceOf(ConflitUnicite.class)
                .hasMessage("La contenance 0,70 L existe déjà.")
                .extracting(e -> ((ConflitUnicite) e).regle().orElseThrow()).isEqualTo("RG-REF-10");
    }

    @Test
    @DisplayName("RG-REF-02 : modifier ne change que le libellé ; un libellé vide revient au libellé dérivé")
    void rg_ref_02_modifier_le_libelle() {
        gerer.creer(new BigDecimal("0.7"), "Bouteille");

        assertThat(gerer.modifier(new BigDecimal("0.70"), " ").libelle()).isEqualTo("0,70 L");
        assertThat(gerer.consulter(new BigDecimal("0.7")).volume()).isEqualTo(Volume.de("0.7"));
    }

    @Test
    @DisplayName("RG-REF-04 : une contenance désactivée reste consultable")
    void rg_ref_04_desactivee_consultable() {
        gerer.creer(new BigDecimal("1.5"), null);

        gerer.desactiver(new BigDecimal("1.5"));

        assertThat(gerer.consulter(new BigDecimal("1.500")).estActif()).isFalse();
        assertThatThrownBy(() -> gerer.consulter(new BigDecimal("2"))).isInstanceOf(ElementIntrouvable.class);
    }

    private static final class ContenancesEnMemoire implements ContenanceRepository, ContenancesLecture {

        private final Map<Volume, Contenance> contenances = new HashMap<>();

        @Override
        public Optional<Contenance> parVolume(Volume volume) {
            return Optional.ofNullable(contenances.get(volume));
        }

        @Override
        public boolean existeVolume(Volume volume) {
            return contenances.containsKey(volume);
        }

        @Override
        public void inserer(Contenance contenance) {
            contenances.put(contenance.volume(), contenance);
        }

        @Override
        public void mettreAJour(Contenance contenance) {
            contenances.put(contenance.volume(), contenance);
        }

        @Override
        public List<ContenanceLigne> lister(FiltreValeurs filtre) {
            throw new UnsupportedOperationException();
        }
    }
}
