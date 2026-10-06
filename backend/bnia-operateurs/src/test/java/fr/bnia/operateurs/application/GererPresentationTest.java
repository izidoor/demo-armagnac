package fr.bnia.operateurs.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.Portee;
import fr.bnia.operateurs.domain.PresentationOperateur;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import fr.bnia.referentiels.domain.partage.ValeurVue;
import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GererPresentationTest {

    private static final long MARQUE = 3;

    private final OperateursEnMemoire operateurs = new OperateursEnMemoire();
    private final ReferentielsLecture referentiels = mock(ReferentielsLecture.class);
    private final PresentationsEnMemoire presentations = new PresentationsEnMemoire();
    private final GererPresentation gerer = new GererPresentation(presentations, operateurs, referentiels);
    private long condom;

    @BeforeEach
    void operateur() {
        condom = new GererOperateurs(operateurs, operateurs, operateurs, referentiels, new JournalEnMemoire(),
                new EvenementsCaptures(), Clock.systemUTC())
                .creer(GererOperateursTest.saisie(TypeOperateur.PRODUCTEUR, "000012", "Maison Durand", "32100", null)).id();
        when(referentiels.marque(MARQUE)).thenReturn(Optional.of(new ValeurVue(MARQUE, "DEMO-1", "Domaine de la Démonstration", true)));
    }

    private static SaisiePresentation saisie(long marque) {
        return new SaisiePresentation(List.of(new SaisiePresentation.Marque(Portee.FRANCE, marque, true)), List.of(),
                List.of(), List.of(), false, Set.of(), false, false, false, false, Set.of(), Set.of(), null, null, null,
                List.of());
    }

    @Test
    @DisplayName("RG-OPE-21 : une marque choisie doit être active ; celle déjà portée reste acceptée")
    void rg_ope_21_marque_active() {
        gerer.modifier(condom, saisie(MARQUE));
        when(referentiels.marque(MARQUE)).thenReturn(Optional.of(new ValeurVue(MARQUE, "DEMO-1", "Domaine de la Démonstration", false)));
        when(referentiels.marque(4)).thenReturn(Optional.of(new ValeurVue(4, "DEMO-2", "Château Exemple", false)));

        assertThat(gerer.modifier(condom, saisie(MARQUE)).marques()).hasSize(1);
        assertThatThrownBy(() -> gerer.modifier(condom, saisie(4)))
                .isInstanceOf(RegleEnfreinte.class).hasMessageContaining("Château Exemple")
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-21");
    }

    @Test
    void vide_tant_que_rien_n_est_saisi_et_introuvable_sans_operateur() {
        assertThat(gerer.consulter(condom).marques()).isEmpty();
        assertThatThrownBy(() -> gerer.consulter(999)).isInstanceOf(ElementIntrouvable.class);
    }

    static class PresentationsEnMemoire implements PresentationRepository {

        final Map<Long, PresentationOperateur> presentations = new HashMap<>();

        @Override
        public Optional<PresentationOperateur> parId(long operateur) {
            return Optional.ofNullable(presentations.get(operateur));
        }

        @Override
        public void enregistrer(PresentationOperateur presentation) {
            presentations.put(presentation.id(), presentation);
        }
    }
}
