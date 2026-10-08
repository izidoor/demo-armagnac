package fr.bnia.operateurs.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.ModificationConcurrente;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.ActionJournal;
import fr.bnia.operateurs.domain.ChampJournal;
import fr.bnia.operateurs.domain.Changement;
import fr.bnia.operateurs.domain.Contact;
import fr.bnia.operateurs.domain.StatutInvitation;
import fr.bnia.operateurs.domain.TypeFiche;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.Role;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GererContactsTest {

    private final Memoire memoire = new Memoire();
    private final GererContacts gerer = memoire.gererContacts();
    private final long durand = memoire.operateur("000001", "Maison Durand");
    private final Acteur jeanne = memoire.membre(durand, Role.ADMIN, "Jeanne", "Durand");
    private final Acteur sophie = memoire.membre(durand, Role.AGENT, "Sophie", "Martin");
    private final Acteur pierre = memoire.membre(durand, Role.OBSERVATEUR, "Pierre", "Roy");

    private static SaisieContact saisie(String prenom, String nom, String email) {
        return new SaisieContact(Memoire.TITRE, nom, prenom, email, "Maître de chai", "05 62 00 00 01", null, null, null,
                null, null, Set.of(), null);
    }

    @Test
    @DisplayName("RG-OPE-27, 30 et 34 : un contact déclaré a sa fiche professionnelle, et l'action est journalisée")
    void rg_ope_30_contact_et_sa_fiche() {
        Contact contact = gerer.creer(sophie, durand, saisie("Paul", "Petit", null));

        FicheContact fiche = gerer.consulter(pierre, durand, contact.id());
        assertThat(fiche.fiche().type()).isEqualTo(TypeFiche.CONTACT);
        assertThat(fiche.fiche().coordonnees().fonction()).isEqualTo("Maître de chai");
        assertThat(fiche.fiche().modifieePar()).isEqualTo("Sophie Martin");
        assertThat(memoire.journal.derniere()).satisfies(l -> {
            assertThat(l.action()).isEqualTo(ActionJournal.CONTACT_CREE);
            assertThat(l.nomActeur()).isEqualTo("Sophie Martin");
            assertThat(l.nomCible()).isEqualTo("Paul Petit");
            assertThat(l.operateur()).isEqualTo(durand);
        });
        assertThatThrownBy(() -> gerer.creer(sophie, durand, saisie("Paul", " ", null)))
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-27");
    }

    @Test
    @DisplayName("RG-OPE-31 : un OBSERVATEUR lit les contacts mais n'en déclare pas ; un autre opérateur ne les voit pas")
    void rg_ope_31_refus() {
        Contact contact = gerer.creer(jeanne, durand, saisie("Paul", "Petit", null));
        Acteur autre = memoire.membre(memoire.operateur("000002", "Domaine B"), Role.ADMIN, "Marc", "Lafitte");

        assertThatThrownBy(() -> gerer.creer(pierre, durand, saisie("Léa", "Roux", null)))
                .isInstanceOf(AccesRefuse.class).hasMessageContaining("Contacts");
        assertThatThrownBy(() -> gerer.supprimer(pierre, durand, contact.id())).isInstanceOf(AccesRefuse.class);
        assertThatThrownBy(() -> gerer.consulter(autre, durand, contact.id())).isInstanceOf(AccesRefuse.class);
    }

    @Test
    @DisplayName("RG-OPE-28 : l'e-mail est unique parmi les contacts de l'opérateur ; celui d'un membre ou d'une invitation avertit")
    void rg_ope_28_unicite_et_avertissements() {
        long autre = memoire.operateur("000002", "Domaine B");
        Acteur marc = memoire.membre(autre, Role.ADMIN, "Marc", "Lafitte");
        gerer.creer(sophie, durand, saisie("Paul", "Petit", "paul@durand.fr"));

        assertThatThrownBy(() -> gerer.creer(sophie, durand, saisie("Paul", "Petit bis", "PAUL@durand.fr")))
                .isInstanceOf(ConflitUnicite.class).hasMessageContaining("PAUL@durand.fr");
        assertThat(gerer.creer(marc, autre, saisie("Paul", "Petit", "paul@durand.fr")).id())
                .as("deux opérateurs, deux contacts indépendants").isNotNull();
        assertThat(gerer.avertissements(sophie, durand, "jeanne.durand@exemple.fr", null))
                .singleElement().asString().contains("Jeanne Durand, membre de cet opérateur");
        memoire.gererInvitations().inviter(jeanne, durand, SaisieInvitation.personne("lea@roux.fr", Role.AGENT, "Roux", "Léa"));
        assertThat(gerer.avertissements(sophie, durand, "lea@roux.fr", null)).singleElement().asString()
                .contains("invitation en attente");
        assertThat(gerer.avertissements(sophie, durand, "lea@", null)).as("saisie en cours").isEmpty();
    }

    @Test
    @DisplayName("RG-OPE-34 et RG-OPE-51 : modifier un contact journalise chaque champ changé, sur la version lue")
    void rg_ope_34_modification() {
        Contact contact = gerer.creer(sophie, durand, saisie("Paul", "Petit", null));

        gerer.modifier(sophie, durand, contact.id(), new SaisieIdentiteContact(Memoire.TITRE, "Petit", "Paul",
                "paul@durand.fr", 0L));

        assertThat(memoire.journal.derniere().changements())
                .containsExactly(new Changement(ChampJournal.EMAIL, null, "paul@durand.fr"));
        assertThatThrownBy(() -> gerer.modifier(sophie, durand, contact.id(), new SaisieIdentiteContact(null, "Petit",
                "Paul", null, 0L))).isInstanceOf(ModificationConcurrente.class);
    }

    @Test
    @DisplayName("RG-OPE-22, 23 et 45 : supprimer un contact annule ses invitations en attente et fait tomber ses désignations")
    void rg_ope_45_suppression() {
        Contact contact = gerer.creer(sophie, durand, saisie("Paul", "Petit", "paul@durand.fr"));
        long fiche = contact.fiche();
        memoire.gererOperateurs().designer(sophie, durand, new SaisieDesignations(fiche, fiche, null, null));
        memoire.gererInvitations().inviter(jeanne, durand, new SaisieInvitation(contact.id(), "paul@durand.fr",
                Role.AGENT, null, null, null, null, null));
        assertThatThrownBy(() -> gerer.modifier(sophie, durand, contact.id(), new SaisieIdentiteContact(null, "Petit",
                "Paul", " ", null))).as("RG-OPE-23 : l'e-mail du destinataire est requis").hasMessageContaining("factures");

        gerer.supprimer(sophie, durand, contact.id());

        assertThat(memoire.contacts.parId(contact.id())).isEmpty();
        assertThat(memoire.fiches.parId(fiche)).isEmpty();
        assertThat(memoire.invitations.tous()).extracting(i -> i.statut()).containsExactly(StatutInvitation.ANNULEE);
        assertThat(memoire.operateurs.parId(durand).orElseThrow().contactPrincipal()).isEmpty();
        assertThat(memoire.operateurs.parId(durand).orElseThrow().destinataireFactures()).isEmpty();
        assertThat(memoire.journal.derniere().action()).isEqualTo(ActionJournal.CONTACT_SUPPRIME);
    }
}
