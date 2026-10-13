package fr.bnia.operateurs.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.ActionJournal;
import fr.bnia.operateurs.domain.Contact;
import fr.bnia.operateurs.domain.Invitation;
import fr.bnia.operateurs.domain.Rattachement;
import fr.bnia.operateurs.domain.StatutInvitation;
import fr.bnia.operateurs.domain.StatutRattachement;
import fr.bnia.operateurs.domain.TypeFiche;
import fr.bnia.operateurs.domain.Utilisateur;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.operateurs.domain.partage.UtilisateurCree;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GererInvitationsTest {

    private final Memoire memoire = new Memoire();
    private final GererInvitations gerer = memoire.gererInvitations();
    private final long durand = memoire.operateur("000001", "Maison Durand");
    private final Acteur jeanne = memoire.membre(durand, Role.ADMIN, "Jeanne", "Durand");
    private final Acteur sophie = memoire.membre(durand, Role.AGENT, "Sophie", "Martin");

    private static final SaisieAcceptation MARC = new SaisieAcceptation(null, "Lafitte", "Marc");

    private Invitation.Emise inviterMarc() {
        return gerer.inviter(jeanne, durand, new SaisieInvitation(null, "marc@lafitte.fr", Role.AGENT, null, "Lafitte",
                "Marc", "Distillateur", "06 00 00 00 00"));
    }

    @Test
    @DisplayName("RG-OPE-31 et RG-OPE-40 : inviter relève du module Accès ; un AGENT est refusé")
    void rg_ope_40_refus() {
        assertThatThrownBy(() -> gerer.inviter(sophie, durand, SaisieInvitation.personne("marc@lafitte.fr", Role.AGENT,
                "Lafitte", "Marc"))).isInstanceOf(AccesRefuse.class).hasMessageContaining("Accès");
    }

    @Test
    @DisplayName("RG-OPE-34 et RG-OPE-40 : une invitation ne crée personne ; le lien part par e-mail, l'action au journal")
    void rg_ope_40_inviter() {
        Invitation.Emise emise = inviterMarc();

        assertThat(emise.invitation().statut()).isEqualTo(StatutInvitation.EN_ATTENTE);
        assertThat(memoire.utilisateurs.tous()).hasSize(2);
        assertThat(memoire.contacts.tous()).isEmpty();
        assertThat(memoire.courriels.jeton("marc@lafitte.fr")).isEqualTo(emise.jeton());
        assertThat(memoire.courriels.envoyes.getLast().texte()).contains("Maison Durand", "Agent", "Jeanne Durand");
        assertThat(memoire.journal.derniere().action()).isEqualTo(ActionJournal.INVITATION_ENVOYEE);
    }

    @Test
    @DisplayName("RG-OPE-40 : refusée si une invitation est en attente pour cet e-mail ou ce contact, ou si l'e-mail est d'un membre")
    void rg_ope_40_doublons() {
        inviterMarc();
        Contact paul = memoire.contact(durand, "Paul", "Petit", null);
        gerer.inviter(jeanne, durand, new SaisieInvitation(paul.id(), "paul@durand.fr", Role.OBSERVATEUR, null, null,
                null, null, null));

        assertThatThrownBy(() -> gerer.inviter(jeanne, durand, SaisieInvitation.personne("MARC@lafitte.fr", Role.ADMIN,
                "Lafitte", "Marc"))).isInstanceOf(ConflitUnicite.class).hasMessageContaining("relancez-la");
        assertThatThrownBy(() -> gerer.inviter(jeanne, durand, new SaisieInvitation(paul.id(), "petit@durand.fr",
                Role.OBSERVATEUR, null, null, null, null, null))).hasMessageContaining("Paul Petit");
        assertThatThrownBy(() -> gerer.inviter(jeanne, durand, SaisieInvitation.personne("sophie.martin@exemple.fr",
                Role.ADMIN, "Martin", "Sophie"))).hasMessageContaining("déjà membre");
        assertThat(memoire.contacts.parId(paul.id()).orElseThrow().email().valeur())
                .as("partant d'un contact, son e-mail est mis à jour").isEqualTo("paul@durand.fr");
    }

    @Test
    @DisplayName("RG-OPE-41 : relancer invalide l'ancien lien ; annuler ; une invitation échue expire")
    void rg_ope_41_relance_annulation_expiration() {
        Invitation.Emise premiere = inviterMarc();
        Invitation.Emise relance = gerer.relancer(jeanne, durand, premiere.invitation().id());

        assertThatThrownBy(() -> gerer.consulterParJeton(premiere.jeton())).isInstanceOf(ElementIntrouvable.class);
        assertThat(gerer.consulterParJeton(relance.jeton()).nom()).isEqualTo("Lafitte");

        memoire.horloge.avancer(Duration.ofDays(8));
        assertThat(gerer.expirerEchues()).isEqualTo(1);
        assertThat(memoire.invitations.parId(premiere.invitation().id()).orElseThrow().statut())
                .isEqualTo(StatutInvitation.EXPIREE);
        assertThatThrownBy(() -> gerer.accepter(relance.jeton(), Optional.empty(), MARC)).hasMessageContaining("expiré");

        Invitation.Emise autre = inviterMarc();
        gerer.annuler(jeanne, durand, autre.invitation().id());
        assertThat(memoire.journal.derniere().action()).isEqualTo(ActionJournal.INVITATION_ANNULEE);
    }

    @Test
    @DisplayName("RG-OPE-29, 42 et 49 : accepter crée l'utilisateur (événement), sa fiche, son rattachement actif ; il devient l'opérateur actif")
    void rg_ope_42_personne_nouvelle() {
        Invitation.Emise emise = inviterMarc();
        assertThat(gerer.consulterParJeton(emise.jeton()).compteExistant()).isFalse();

        Acceptation acceptation = gerer.accepter(emise.jeton(), Optional.empty(), new SaisieAcceptation(null, "Lafitte",
                "Marc-Antoine"));

        Utilisateur marc = memoire.utilisateurs.parId(acceptation.utilisateur()).orElseThrow();
        assertThat(acceptation.nouveauCompte()).isTrue();
        assertThat(marc.identite().prenom()).as("confirmé par la personne").isEqualTo("Marc-Antoine");
        assertThat(marc.operateurActif()).contains(durand);
        Rattachement rattachement = memoire.rattachements.parId(acceptation.rattachement()).orElseThrow();
        assertThat(rattachement.role()).isEqualTo(Role.AGENT);
        assertThat(rattachement.statut()).isEqualTo(StatutRattachement.ACTIF);
        assertThat(memoire.fiches.parId(rattachement.fiche()).orElseThrow().coordonnees().fonction()).isEqualTo("Distillateur");
        assertThat(memoire.evenements.publies).containsExactly(new UtilisateurCree(marc.id(), "marc@lafitte.fr"));
        assertThat(memoire.invitations.parId(emise.invitation().id()).orElseThrow().statut()).isEqualTo(StatutInvitation.ACCEPTEE);
        assertThat(gerer.utilisateurDInvitationAcceptee(emise.jeton())).as("RG-AUT-15").contains(marc.id());
        memoire.horloge.avancer(Duration.ofHours(2));
        assertThat(gerer.utilisateurDInvitationAcceptee(emise.jeton())).isEmpty();
    }

    @Test
    @DisplayName("RG-OPE-30 et RG-OPE-42 : partant d'un contact, le contact disparaît et sa fiche passe au membre")
    void rg_ope_42_depuis_un_contact() {
        Contact paul = memoire.contact(durand, "Paul", "Petit", "paul@durand.fr");
        Invitation.Emise emise = gerer.inviter(jeanne, durand, new SaisieInvitation(paul.id(), "paul@durand.fr",
                Role.OBSERVATEUR, null, null, null, null, null));
        assertThat(gerer.consulterParJeton(emise.jeton()).prenom()).as("prérempli depuis le contact").isEqualTo("Paul");

        Acceptation acceptation = gerer.accepter(emise.jeton(), Optional.empty(), new SaisieAcceptation(null, "Petit", "Paul"));

        assertThat(memoire.contacts.parId(paul.id())).isEmpty();
        Rattachement rattachement = memoire.rattachements.parId(acceptation.rattachement()).orElseThrow();
        assertThat(rattachement.fiche()).isEqualTo(paul.fiche());
        assertThat(memoire.fiches.parId(paul.fiche()).orElseThrow().type()).isEqualTo(TypeFiche.RATTACHEMENT);
    }

    @Test
    @DisplayName("RG-OPE-29 et RG-OPE-40 : une personne nouvelle à l'e-mail d'un contact est invitée depuis ce contact")
    void rg_ope_40_personne_nouvelle_a_l_email_d_un_contact() {
        Contact paul = memoire.contact(durand, "Paul", "Petit", "paul@durand.fr");

        Invitation.Emise emise = gerer.inviter(jeanne, durand, SaisieInvitation.personne("PAUL@durand.fr",
                Role.OBSERVATEUR, "Petit2", "Paul2"));

        assertThat(emise.invitation().fiche()).contains(paul.fiche());
        assertThat(emise.invitation().personne()).as("l'identité saisie est ignorée").isEmpty();
        assertThat(gerer.consulterParJeton(emise.jeton()).prenom()).isEqualTo("Paul");
        assertThatThrownBy(() -> gerer.inviter(jeanne, durand, new SaisieInvitation(paul.id(), "paul@durand.fr",
                Role.AGENT, null, null, null, null, null))).hasMessageContaining("relancez-la");

        Acceptation acceptation = gerer.accepter(emise.jeton(), Optional.empty(), new SaisieAcceptation(null, "Petit",
                "Paul"));

        assertThat(memoire.contacts.parId(paul.id())).as("une seule personne dans l'opérateur").isEmpty();
        assertThat(memoire.rattachements.parId(acceptation.rattachement()).orElseThrow().fiche()).isEqualTo(paul.fiche());
    }

    @Test
    @DisplayName("RG-OPE-29 et RG-OPE-42 : un contact déclaré pendant l'attente avec l'e-mail de l'invitation devient le membre")
    void rg_ope_42_contact_declare_pendant_l_attente() {
        Invitation.Emise emise = inviterMarc();
        Contact marc = memoire.contact(durand, "Marc", "Lafitte", "marc@lafitte.fr");

        Acceptation acceptation = gerer.accepter(emise.jeton(), Optional.empty(), MARC);

        assertThat(memoire.contacts.parId(marc.id())).isEmpty();
        Rattachement rattachement = memoire.rattachements.parId(acceptation.rattachement()).orElseThrow();
        assertThat(rattachement.fiche()).isEqualTo(marc.fiche());
        assertThat(memoire.fiches.parId(marc.fiche()).orElseThrow().type()).isEqualTo(TypeFiche.RATTACHEMENT);
    }

    @Test
    @DisplayName("RG-OPE-42 : un compte existant se connecte pour accepter, garde son identité, et n'accepte pas deux fois")
    void rg_ope_42_utilisateur_existant() {
        long eauze = memoire.operateur("000002", "Maison Durand, Eauze");
        Acteur admin = memoire.membre(eauze, Role.ADMIN, "Anne", "Lamarque");
        Invitation.Emise emise = gerer.inviter(admin, eauze, SaisieInvitation.personne("jeanne.durand@exemple.fr",
                Role.ADMIN, "Durand", "Jeanne-Marie"));
        assertThat(gerer.consulterParJeton(emise.jeton()).compteExistant()).isTrue();

        assertThatThrownBy(() -> gerer.accepter(emise.jeton(), Optional.empty(), MARC))
                .isInstanceOf(AccesRefuse.class).hasMessageContaining("connectez-vous");
        assertThatThrownBy(() -> gerer.accepter(emise.jeton(), Optional.of(sophie), MARC)).isInstanceOf(AccesRefuse.class);
        Acceptation acceptation = gerer.accepter(emise.jeton(), Optional.of(jeanne), MARC);

        assertThat(acceptation.nouveauCompte()).isFalse();
        assertThat(memoire.utilisateurs.parId(jeanne.utilisateur()).orElseThrow().identite().prenom()).isEqualTo("Jeanne");
        assertThat(memoire.utilisateurs.parId(jeanne.utilisateur()).orElseThrow().operateurActif()).contains(durand);
        assertThat(memoire.evenements.publies).as("aucun nouveau compte").isEmpty();

        long laubade = memoire.operateur("000003", "Domaine de Laubade");
        Invitation.Emise seconde = memoire.gererInvitations().emettre(null, laubade,
                SaisieInvitation.personne("jeanne.durand@exemple.fr", Role.AGENT, "Durand", "Jeanne"));
        memoire.membre(laubade, Role.OBSERVATEUR, "Jeanne", "Durand");
        assertThatThrownBy(() -> gerer.accepter(seconde.jeton(), Optional.of(jeanne), MARC))
                .isInstanceOf(RegleEnfreinte.class).hasMessageContaining("déjà membre");
    }

    @Test
    @DisplayName("RG-OPE-42 et RG-OPE-48 : une invitation adressée à un e-mail qu'un utilisateur a quitté ne s'accepte plus")
    void rg_ope_42_email_quitte() {
        long eauze = memoire.operateur("000002", "Maison Durand, Eauze");
        Acteur admin = memoire.membre(eauze, Role.ADMIN, "Anne", "Lamarque");
        Invitation.Emise emise = gerer.inviter(admin, eauze, SaisieInvitation.personne("sophie.martin@exemple.fr",
                Role.AGENT, "Martin", "Sophie"));
        memoire.monCompte().demanderChangementEmail(sophie, "sophie@martin.fr");
        memoire.monCompte().confirmerChangementEmail(memoire.courriels.jeton("sophie@martin.fr"));

        assertThatThrownBy(() -> gerer.consulterParJeton(emise.jeton())).hasMessageContaining("changé d'e-mail");
        assertThatThrownBy(() -> gerer.accepter(emise.jeton(), Optional.empty(), MARC))
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-42");
    }
}
