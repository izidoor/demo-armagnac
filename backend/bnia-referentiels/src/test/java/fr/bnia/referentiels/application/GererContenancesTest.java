package fr.bnia.referentiels.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.referentiels.domain.Contenance;
import fr.bnia.referentiels.domain.Volume;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
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
    @DisplayName("RG-REF-02 : le volume se corrige, toujours unique (RG-REF-10)")
    void rg_ref_02_le_volume_se_corrige() {
        long erreur = gerer.creer(new BigDecimal("0.07"), "Bouteille").id();
        gerer.creer(new BigDecimal("1.5"), null);

        var corrigee = gerer.modifier(erreur, new BigDecimal("0.70"), " ");

        assertThat(corrigee.id()).isEqualTo(erreur);
        assertThat(corrigee.volume()).isEqualTo(Volume.de("0.7"));
        assertThat(corrigee.libelle()).isEqualTo("0,70 L");
        assertThatThrownBy(() -> gerer.modifier(erreur, new BigDecimal("1.500"), null)).isInstanceOf(ConflitUnicite.class);
    }

    @Test
    @DisplayName("RG-REF-04 : une contenance désactivée reste consultable ; une contenance inutilisée se supprime")
    void rg_ref_04_desactiver_et_supprimer() {
        long id = gerer.creer(new BigDecimal("1.5"), null).id();

        gerer.desactiver(id);
        assertThat(gerer.consulter(id).estActif()).isFalse();

        gerer.supprimer(id);
        assertThatThrownBy(() -> gerer.consulter(id)).isInstanceOf(ElementIntrouvable.class);
    }

    private static final class ContenancesEnMemoire implements ContenanceRepository, ContenancesLecture {

        private final Map<Long, Contenance> contenances = new LinkedHashMap<>();
        private long prochainId = 1;

        @Override
        public Optional<Contenance> parId(long id) {
            return Optional.ofNullable(contenances.get(id));
        }

        @Override
        public boolean existeVolume(Volume volume, Long sauf) {
            return contenances.values().stream().anyMatch(c -> c.volume().equals(volume) && !c.id().equals(sauf));
        }

        @Override
        public Contenance inserer(Contenance contenance) {
            var enregistree = Contenance.reconstituer(prochainId++, contenance.volume(),
                    contenance.libelleSaisi().orElse(null), contenance.estActif());
            contenances.put(enregistree.id(), enregistree);
            return enregistree;
        }

        @Override
        public void mettreAJour(Contenance contenance) {
            contenances.put(contenance.id(), contenance);
        }

        @Override
        public void supprimer(long id) {
            contenances.remove(id);
        }

        @Override
        public List<ContenanceLigne> lister(FiltreValeurs filtre) {
            throw new UnsupportedOperationException();
        }
    }
}
