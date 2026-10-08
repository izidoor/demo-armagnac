package fr.bnia.operateurs.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ModificationConcurrente;
import fr.bnia.operateurs.domain.ActionJournal;
import fr.bnia.operateurs.domain.Contact;
import fr.bnia.operateurs.domain.TypeCible;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.Role;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GererFichesTest {

    private final Memoire memoire = new Memoire();
    private final GererFiches gerer = memoire.gererFiches();
    private final long durand = memoire.operateur("000001", "Maison Durand");
    private final Acteur pierre = memoire.membre(durand, Role.OBSERVATEUR, "Pierre", "Roy");
    private final Acteur sophie = memoire.membre(durand, Role.AGENT, "Sophie", "Martin");

    private static SaisieFiche saisie(String fonction, Long version) {
        return new SaisieFiche(fonction, "05 62 00 00 09", null, null, null, null, Memoire.FRANCE, Set.of(), null, version);
    }

    @Test
    @DisplayName("RG-OPE-33 et RG-OPE-46 : chacun modifie sa propre fiche, sans rôle particulier")
    void rg_ope_46_sa_propre_fiche() {
        long fiche = memoire.rattachementDe(pierre, durand).fiche();

        var modifiee = gerer.modifier(pierre, durand, fiche, saisie("Caviste", 0L));

        assertThat(modifiee.coordonnees().fonction()).isEqualTo("Caviste");
        assertThat(modifiee.modifieePar()).isEqualTo("Pierre Roy");
        assertThat(memoire.journal.derniere()).satisfies(l -> {
            assertThat(l.action()).isEqualTo(ActionJournal.FICHE_MODIFIEE);
            assertThat(l.cible()).isEqualTo(TypeCible.MEMBRE);
            assertThat(l.nomCible()).isEqualTo("Pierre Roy");
        });
    }

    @Test
    @DisplayName("RG-OPE-46 : la fiche d'un autre se modifie par le module Contacts ; un OBSERVATEUR est refusé")
    void rg_ope_46_fiche_d_un_autre() {
        Contact paul = memoire.contact(durand, "Paul", "Petit", null);
        long ficheSophie = memoire.rattachementDe(sophie, durand).fiche();

        assertThatThrownBy(() -> gerer.modifier(pierre, durand, ficheSophie, saisie("Gérante", null)))
                .isInstanceOf(AccesRefuse.class).hasMessageContaining("votre propre fiche");
        assertThat(gerer.modifier(sophie, durand, paul.fiche(), saisie("Chef de culture", null)).coordonnees().fonction())
                .isEqualTo("Chef de culture");
        assertThat(memoire.journal.derniere().cible()).isEqualTo(TypeCible.CONTACT);
    }

    @Test
    @DisplayName("RG-OPE-33 : un membre suspendu ne modifie plus sa fiche")
    void rg_ope_33_suspendu() {
        var rattachement = memoire.rattachementDe(pierre, durand);
        rattachement.suspendre();
        memoire.rattachements.mettreAJour(rattachement);

        assertThatThrownBy(() -> gerer.modifier(pierre, durand, rattachement.fiche(), saisie("Caviste", null)))
                .isInstanceOf(AccesRefuse.class);
    }

    @Test
    @DisplayName("RG-OPE-51 : une fiche modifiée entre-temps n'est pas écrasée")
    void rg_ope_51_conflit() {
        long fiche = memoire.rattachementDe(pierre, durand).fiche();
        gerer.modifier(sophie, durand, fiche, saisie("Caviste", 0L));

        assertThatThrownBy(() -> gerer.modifier(pierre, durand, fiche, saisie("Maître de chai", 0L)))
                .isInstanceOf(ModificationConcurrente.class).hasMessageContaining("Pierre Roy");
    }
}
