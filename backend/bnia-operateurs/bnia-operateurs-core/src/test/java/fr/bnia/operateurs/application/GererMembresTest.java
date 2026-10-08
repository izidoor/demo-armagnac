package fr.bnia.operateurs.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.ActionJournal;
import fr.bnia.operateurs.domain.Contact;
import fr.bnia.operateurs.domain.Rattachement;
import fr.bnia.operateurs.domain.StatutRattachement;
import fr.bnia.operateurs.domain.TypeFiche;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.EtatConnexion;
import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.operateurs.domain.partage.UtilisateurSupprime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GererMembresTest {

    private final Memoire memoire = new Memoire();
    private final GererMembres gerer = memoire.gererMembres();
    private final long durand = memoire.operateur("000001", "Maison Durand");
    private final Acteur jeanne = memoire.membre(durand, Role.ADMIN, "Jeanne", "Durand");
    private final Acteur sophie = memoire.membre(durand, Role.AGENT, "Sophie", "Martin");
    private final Acteur anne = memoire.membre(memoire.bnia, Role.ADMIN, "Anne", "Lamarque");

    private long rattachement(Acteur acteur, long operateur) {
        return memoire.rattachementDe(acteur, operateur).id();
    }

    @Test
    @DisplayName("RG-OPE-31 et RG-OPE-43 : changer un rôle relève du module Accès ; un AGENT est refusé")
    void rg_ope_43_refus() {
        assertThatThrownBy(() -> gerer.changerRole(sophie, durand, rattachement(jeanne, durand), Role.AGENT))
                .isInstanceOf(AccesRefuse.class);
        assertThatThrownBy(() -> gerer.retirer(sophie, durand, rattachement(sophie, durand), false))
                .as("RG-OPE-44 : on ne quitte pas un opérateur de soi-même").isInstanceOf(AccesRefuse.class);
    }

    @Test
    @DisplayName("RG-OPE-34 et RG-OPE-43 : changer le rôle, suspendre, réactiver ; effet immédiat dans cet opérateur seul")
    void rg_ope_43_role_et_suspension() {
        long eauze = memoire.operateur("000002", "Maison Durand, Eauze");
        memoire.membre(eauze, Role.AGENT, "Sophie", "Martin");
        long idSophie = rattachement(sophie, durand);

        assertThat(gerer.changerRole(jeanne, durand, idSophie, Role.ADMIN).role()).isEqualTo(Role.ADMIN);
        assertThat(memoire.journal.derniere().action()).isEqualTo(ActionJournal.ROLE_CHANGE);
        gerer.suspendre(jeanne, durand, idSophie);

        assertThat(memoire.habilitations().droits(sophie, durand).aucunAcces()).isTrue();
        assertThat(memoire.habilitations().droits(sophie, eauze).aucunAcces()).as("autre opérateur inchangé").isFalse();
        assertThat(memoire.journal.derniere().action()).isEqualTo(ActionJournal.ACCES_SUSPENDU);
        assertThat(gerer.reactiver(jeanne, durand, idSophie).statut()).isEqualTo(StatutRattachement.ACTIF);
        assertThat(memoire.operateurs.verrous).as("A-OPE-6 : verrou sur l'opérateur").contains(durand);
    }

    @Test
    @DisplayName("RG-OPE-43 : un utilisateur sans aucun rattachement utilisable ne peut plus se connecter")
    void rg_ope_43_plus_aucun_acces() {
        gerer.suspendre(jeanne, durand, rattachement(sophie, durand));

        assertThat(memoire.habilitations().etatConnexion(sophie.utilisateur())).isEqualTo(EtatConnexion.AUCUN_ACCES_ACTIF);
    }

    @Test
    @DisplayName("RG-OPE-35 : le dernier ADMIN n'est ni rétrogradé, ni suspendu, ni retiré par un membre ; un ADMIN du BNIA le peut")
    void rg_ope_35_dernier_admin() {
        long idJeanne = rattachement(jeanne, durand);

        assertThatThrownBy(() -> gerer.changerRole(jeanne, durand, idJeanne, Role.AGENT))
                .isInstanceOf(RegleEnfreinte.class).hasMessageContaining("dernier ADMIN")
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-35");
        assertThatThrownBy(() -> gerer.suspendre(jeanne, durand, idJeanne)).hasMessageContaining("adressez-vous au BNIA");
        assertThat(gerer.consulter(sophie, durand, idJeanne).dernierAdmin()).isTrue();

        gerer.changerRole(jeanne, durand, rattachement(sophie, durand), Role.ADMIN);
        assertThat(gerer.retirer(jeanne, durand, idJeanne, true)).as("un ADMIN se retire s'il n'est pas le dernier")
                .isPresent();
        assertThat(gerer.suspendre(anne, durand, rattachement(sophie, durand)).statut()).isEqualTo(StatutRattachement.SUSPENDU);
    }

    @Test
    @DisplayName("RG-OPE-35 : pour l'opérateur BNIA, la règle est absolue")
    void rg_ope_35_bnia() {
        assertThatThrownBy(() -> gerer.changerRole(anne, memoire.bnia, rattachement(anne, memoire.bnia), Role.AGENT))
                .hasMessageContaining("dernier ADMIN du BNIA");
    }

    @Test
    @DisplayName("RG-OPE-30 et RG-OPE-44 : retirer en conservant la personne en fait un contact, avec la même fiche")
    void rg_ope_44_conserver_contact() {
        long eauze = memoire.operateur("000002", "Maison Durand, Eauze");
        memoire.membre(eauze, Role.AGENT, "Sophie", "Martin");
        Rattachement rattachement = memoire.rattachementDe(sophie, durand);

        Contact contact = gerer.retirer(jeanne, durand, rattachement.id(), true).orElseThrow();

        assertThat(contact.identite().nomComplet()).isEqualTo("Sophie Martin");
        assertThat(contact.email().valeur()).isEqualTo("sophie.martin@exemple.fr");
        assertThat(contact.fiche()).isEqualTo(rattachement.fiche());
        assertThat(memoire.fiches.parId(rattachement.fiche()).orElseThrow().type()).isEqualTo(TypeFiche.CONTACT);
        assertThat(memoire.utilisateurs.parId(sophie.utilisateur())).as("encore membre à Eauze").isPresent();
        assertThat(memoire.evenements.publies).isEmpty();
    }

    @Test
    @DisplayName("RG-OPE-22, 23 et 44 : retirer sans conserver supprime la fiche ; au dernier rattachement, l'utilisateur et son compte")
    void rg_ope_44_dernier_rattachement() {
        Rattachement rattachement = memoire.rattachementDe(sophie, durand);
        memoire.gererOperateurs().designer(jeanne, durand, new SaisieDesignations(rattachement.fiche(),
                rattachement.fiche(), null, null));

        assertThat(gerer.retirer(jeanne, durand, rattachement.id(), false)).isEmpty();

        assertThat(memoire.fiches.parId(rattachement.fiche())).isEmpty();
        assertThat(memoire.operateurs.parId(durand).orElseThrow().contactPrincipal()).isEmpty();
        assertThat(memoire.utilisateurs.parId(sophie.utilisateur())).isEmpty();
        assertThat(memoire.evenements.publies).containsExactly(new UtilisateurSupprime(sophie.utilisateur()));
        assertThat(memoire.journal.lignes).extracting(LigneJournal::action)
                .containsSubsequence(ActionJournal.ACCES_RETIRE, ActionJournal.UTILISATEUR_SUPPRIME);
    }

    @Test
    @DisplayName("RG-OPE-28 et RG-OPE-44 : la personne conservée ne prend pas l'e-mail d'un contact existant")
    void rg_ope_44_email_deja_contact() {
        memoire.contact(durand, "Sophie", "Martin (perso)", "sophie.martin@exemple.fr");

        assertThatThrownBy(() -> gerer.retirer(jeanne, durand, rattachement(sophie, durand), true))
                .isInstanceOf(ConflitUnicite.class).hasMessageContaining("sans conserver");
    }
}
