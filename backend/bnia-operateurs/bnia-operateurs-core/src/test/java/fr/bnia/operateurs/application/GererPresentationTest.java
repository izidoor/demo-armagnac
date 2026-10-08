package fr.bnia.operateurs.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.Portee;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.referentiels.domain.partage.ValeurVue;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GererPresentationTest {

    private static final long MARQUE = 3;

    private final Memoire memoire = new Memoire();
    private final GererPresentation gerer = memoire.gererPresentation();
    private final long condom = memoire.operateur("000012", "Maison Durand");
    private final Acteur sophie = memoire.membre(condom, Role.AGENT, "Sophie", "Martin");
    private final Acteur pierre = memoire.membre(condom, Role.OBSERVATEUR, "Pierre", "Roy");

    @BeforeEach
    void marque() {
        when(memoire.referentiels.marque(MARQUE)).thenReturn(Optional.of(new ValeurVue(MARQUE, "DEMO-1",
                "Domaine de la Démonstration", true)));
    }

    private static SaisiePresentation saisie(long marque) {
        return new SaisiePresentation(List.of(new SaisiePresentation.Marque(Portee.FRANCE, marque, true)), List.of(),
                List.of(), List.of(), false, Set.of(), false, false, false, false, Set.of(), Set.of(), null, null, null,
                List.of());
    }

    @Test
    @DisplayName("RG-OPE-21 : une marque choisie doit être active ; celle déjà portée reste acceptée")
    void rg_ope_21_marque_active() {
        gerer.modifier(sophie, condom, saisie(MARQUE));
        when(memoire.referentiels.marque(MARQUE)).thenReturn(Optional.of(new ValeurVue(MARQUE, "DEMO-1",
                "Domaine de la Démonstration", false)));
        when(memoire.referentiels.marque(4)).thenReturn(Optional.of(new ValeurVue(4, "DEMO-2", "Château Exemple", false)));

        assertThat(gerer.modifier(sophie, condom, saisie(MARQUE)).marques()).hasSize(1);
        assertThatThrownBy(() -> gerer.modifier(sophie, condom, saisie(4)))
                .isInstanceOf(RegleEnfreinte.class).hasMessageContaining("Château Exemple")
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-21");
    }

    @Test
    @DisplayName("RG-OPE-31 : tout membre lit la présentation ; un OBSERVATEUR ne la modifie pas")
    void rg_ope_31_lecture_et_refus() {
        assertThat(gerer.consulter(pierre, condom).marques()).isEmpty();
        assertThatThrownBy(() -> gerer.modifier(pierre, condom, saisie(MARQUE))).isInstanceOf(AccesRefuse.class);
        Acteur anne = memoire.membre(memoire.bnia, Role.ADMIN, "Anne", "Lamarque");
        assertThatThrownBy(() -> gerer.consulter(anne, 999)).isInstanceOf(ElementIntrouvable.class);
        assertThatThrownBy(() -> gerer.modifier(anne, memoire.bnia, saisie(MARQUE)))
                .as("RG-OPE-39 : pas de présentation pour le BNIA").hasMessageContaining("BNIA");
    }
}
