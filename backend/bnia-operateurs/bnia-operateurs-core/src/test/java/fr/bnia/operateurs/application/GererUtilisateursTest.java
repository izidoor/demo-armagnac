package fr.bnia.operateurs.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.operateurs.domain.ActionJournal;
import fr.bnia.operateurs.domain.ChampJournal;
import fr.bnia.operateurs.domain.Changement;
import fr.bnia.operateurs.domain.StatutUtilisateur;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.EtatConnexion;
import fr.bnia.operateurs.domain.partage.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GererUtilisateursTest {

    private final Memoire memoire = new Memoire();
    private final GererUtilisateurs gerer = memoire.gererUtilisateurs();
    private final long condom = memoire.operateur("000001", "Maison Durand, Condom");
    private final long eauze = memoire.operateur("000002", "Maison Durand, Eauze");
    private final Acteur anne = memoire.membre(memoire.bnia, Role.ADMIN, "Anne", "Lamarque");
    private final Acteur luc = memoire.membre(memoire.bnia, Role.AGENT, "Luc", "Dubos");
    private final Acteur jeanne = memoire.membre(condom, Role.ADMIN, "Jeanne", "Durand");
    private final Acteur pierre = memoire.membre(eauze, Role.OBSERVATEUR, "Pierre", "Roy");

    {
        memoire.membre(eauze, Role.ADMIN, "Jeanne", "Durand");
        memoire.membre(condom, Role.AGENT, "Pierre", "Roy");
    }

    @Test
    @DisplayName("RG-OPE-32 et RG-OPE-53 : le personnel du BNIA consulte les utilisateurs ; un membre d'un opérateur est refusé")
    void rg_ope_53_liste() {
        assertThat(gerer.lister(luc, new FiltreUtilisateurs(null, null, null))).hasSize(4);
        assertThat(gerer.consulter(luc, pierre.utilisateur()).rattachements()).hasSize(2);
        assertThatThrownBy(() -> gerer.lister(jeanne, new FiltreUtilisateurs(null, null, null)))
                .isInstanceOf(AccesRefuse.class).hasMessageContaining("personnel du BNIA");
    }

    @Test
    @DisplayName("RG-OPE-34 et RG-OPE-50 : un ADMIN du BNIA bloque avec un motif, au journal ; l'utilisateur et les ADMIN de ses opérateurs sont prévenus sans le motif")
    void rg_ope_50_blocage() {
        gerer.bloquer(anne, pierre.utilisateur(), "Usurpation suspectée");

        assertThat(memoire.utilisateurs.parId(pierre.utilisateur()).orElseThrow().statut()).isEqualTo(StatutUtilisateur.BLOQUE);
        assertThat(memoire.habilitations().etatConnexion(pierre.utilisateur())).isEqualTo(EtatConnexion.UTILISATEUR_BLOQUE);
        assertThat(memoire.habilitations().droits(pierre, eauze).aucunAcces()).as("effet immédiat").isTrue();
        assertThat(memoire.journal.derniere()).satisfies(l -> {
            assertThat(l.action()).isEqualTo(ActionJournal.UTILISATEUR_BLOQUE);
            assertThat(l.operateur()).isNull();
            assertThat(l.changements()).contains(new Changement(ChampJournal.MOTIF, null, "Usurpation suspectée"));
        });
        assertThat(memoire.courriels.envoyes).extracting(Courriel::destinataire)
                .containsExactly("pierre.roy@exemple.fr", "jeanne.durand@exemple.fr");
        assertThat(memoire.courriels.envoyes).allSatisfy(c -> assertThat(c.texte()).doesNotContain("Usurpation"));

        gerer.debloquer(anne, pierre.utilisateur());
        assertThat(memoire.habilitations().etatConnexion(pierre.utilisateur())).isEqualTo(EtatConnexion.AUTORISE);
        assertThat(memoire.courriels.envoyes).hasSize(4);
        assertThat(memoire.journal.derniere().action()).isEqualTo(ActionJournal.UTILISATEUR_DEBLOQUE);
    }

    @Test
    @DisplayName("RG-OPE-50 : bloquer est réservé à un ADMIN du BNIA")
    void rg_ope_50_refus() {
        assertThatThrownBy(() -> gerer.bloquer(luc, pierre.utilisateur(), "motif")).isInstanceOf(AccesRefuse.class);
        assertThatThrownBy(() -> gerer.bloquer(jeanne, pierre.utilisateur(), "motif")).isInstanceOf(AccesRefuse.class);
    }
}
