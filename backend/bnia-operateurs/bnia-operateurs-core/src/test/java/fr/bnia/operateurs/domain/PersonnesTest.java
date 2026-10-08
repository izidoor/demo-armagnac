package fr.bnia.operateurs.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.ModificationConcurrente;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.partage.Role;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PersonnesTest {

    private static final Instant MAINTENANT = Instant.parse("2026-10-08T08:00:00Z");

    private static String regle(Throwable e) {
        return ((RegleEnfreinte) e).regle().orElseThrow();
    }

    @Test
    @DisplayName("RG-OPE-27 : le nom est obligatoire ; le prénom l'est aussi pour un utilisateur")
    void rg_ope_27_identite() {
        assertThatThrownBy(() -> new IdentitePersonne(null, "  ", "Paul")).extracting(PersonnesTest::regle)
                .isEqualTo("RG-OPE-27");
        assertThat(new IdentitePersonne(null, "Petit", null).nomComplet()).as("contact sans prénom").isEqualTo("Petit");
        assertThatThrownBy(() -> IdentitePersonne.utilisateur(null, "Durand", " ")).hasMessageContaining("prénom");
        assertThat(IdentitePersonne.utilisateur(3L, " Durand ", "Jeanne").nomComplet()).isEqualTo("Jeanne Durand");
        assertThat(CoordonneesProfessionnelles.vides().fonction()).as("tout est facultatif dans une fiche").isNull();
    }

    @Test
    @DisplayName("RG-OPE-28 : l'e-mail d'un contact a le format d'une adresse et se compare à la casse près")
    void rg_ope_28_email_contact() {
        assertThatThrownBy(() -> Email.de("paul.petit", "RG-OPE-28")).extracting(PersonnesTest::regle)
                .isEqualTo("RG-OPE-28");
        Contact contact = Contact.creer(1, new IdentitePersonne(null, "Petit", "Paul"),
                Email.de("Paul.Petit@Durand.fr", "RG-OPE-28"), 5);
        assertThat(contact.aEmail(Email.de("paul.petit@durand.fr", "RG-OPE-28"))).isTrue();
        assertThat(contact.mettreAJourEmail(Email.de("PAUL.PETIT@durand.fr", "RG-OPE-40")))
                .as("même adresse à la casse près : inchangé").isFalse();
    }

    @Test
    @DisplayName("RG-OPE-29 : un e-mail désigne un utilisateur, comparé sans distinction de casse")
    void rg_ope_29_email_utilisateur() {
        Utilisateur utilisateur = Utilisateur.creer(IdentitePersonne.utilisateur(null, "Durand", "Jeanne"),
                Email.de("Jeanne.Durand@exemple.fr", "RG-OPE-29"), 7);

        assertThat(utilisateur.email().cle()).isEqualTo("jeanne.durand@exemple.fr");
        assertThat(utilisateur.operateurActif()).as("l'opérateur de l'invitation").contains(7L);
    }

    @Test
    @DisplayName("RG-OPE-30 : la fiche reste la même ligne quand la personne change d'état ; seul son type change")
    void rg_ope_30_fiche_suit_la_personne() {
        FicheProfessionnelle fiche = FicheProfessionnelle.reconstituer(9L, 1, TypeFiche.CONTACT,
                CoordonneesProfessionnelles.de("Maître de chai", "05 62 00 00 00"), "Anne Lamarque", MAINTENANT, 0);

        fiche.porterPar(TypeFiche.RATTACHEMENT);

        assertThat(fiche.id()).isEqualTo(9L);
        assertThat(fiche.type()).isEqualTo(TypeFiche.RATTACHEMENT);
        assertThat(fiche.coordonnees().fonction()).isEqualTo("Maître de chai");
    }

    @Test
    @DisplayName("RG-OPE-34 et RG-OPE-46 : une fiche modifiée garde l'auteur, la date et les champs changés")
    void rg_ope_46_fiche_modifiee() {
        FicheProfessionnelle fiche = FicheProfessionnelle.reconstituer(9L, 1, TypeFiche.RATTACHEMENT,
                CoordonneesProfessionnelles.de("Comptable", null), "Anne Lamarque", MAINTENANT, 2);
        Instant plusTard = MAINTENANT.plusSeconds(60);

        List<Changement> changements = fiche.modifier(new CoordonneesProfessionnelles("Gérante", "05 62 28 00 01", null,
                null, null, null, null, Set.of(4L), null), 2L, "Jeanne Durand", "Jeanne Durand", plusTard);

        assertThat(changements).extracting(Changement::champ).containsExactly(ChampJournal.FONCTION,
                ChampJournal.TELEPHONE_PROFESSIONNEL, ChampJournal.CENTRES_INTERET);
        assertThat(changements.getFirst()).isEqualTo(new Changement(ChampJournal.FONCTION, "Comptable", "Gérante"));
        assertThat(fiche.modifieePar()).isEqualTo("Jeanne Durand");
        assertThat(fiche.modifieeLe()).isEqualTo(plusTard);
    }

    @Test
    @DisplayName("RG-OPE-51 : contact, fiche et utilisateur refusent une modification sur une version dépassée")
    void rg_ope_51_versions() {
        Contact contact = Contact.reconstituer(1L, 1, new IdentitePersonne(null, "Petit", "Paul"), null, 5, 4);
        assertThatThrownBy(() -> contact.modifier(new IdentitePersonne(null, "Petit", "Paul"), null, 3L))
                .isInstanceOf(ModificationConcurrente.class).hasMessageContaining("Paul Petit");
        Utilisateur utilisateur = Utilisateur.reconstituer(1L, IdentitePersonne.utilisateur(null, "Durand", "Jeanne"),
                Email.de("j@d.fr", "t"), StatutUtilisateur.ACTIF, null, null, 1);
        assertThatThrownBy(() -> utilisateur.modifierProfil(IdentitePersonne.utilisateur(null, "Durand", "Jeanne"), 0L))
                .extracting(PersonnesTest::regle).isEqualTo("RG-OPE-51");
    }

    @Test
    @DisplayName("RG-OPE-40 : une invitation exige un e-mail, un rôle, et le prénom d'une personne nouvelle ; son jeton n'est gardé qu'en empreinte")
    void rg_ope_40_emettre() {
        Email email = Email.de("marc@lafitte.fr", "RG-OPE-40");
        assertThatThrownBy(() -> Invitation.emettre(1, null, Role.ADMIN, null, new IdentitePersonne(null, "Lafitte", "Marc"),
                null, null, "Anne", MAINTENANT, Duration.ofDays(7), null)).extracting(PersonnesTest::regle).isEqualTo("RG-OPE-40");
        assertThatThrownBy(() -> Invitation.emettre(1, email, null, null, new IdentitePersonne(null, "Lafitte", "Marc"),
                null, null, "Anne", MAINTENANT, Duration.ofDays(7), null)).hasMessageContaining("rôle");
        assertThatThrownBy(() -> Invitation.emettre(1, email, Role.ADMIN, null, new IdentitePersonne(null, "Lafitte", null),
                null, null, "Anne", MAINTENANT, Duration.ofDays(7), null)).hasMessageContaining("prénom");

        Invitation.Emise emise = Invitation.emettre(1, email, Role.ADMIN, null,
                new IdentitePersonne(null, "Lafitte", "Marc"), null, null, "Anne", MAINTENANT, Duration.ofDays(7), null);

        assertThat(emise.invitation().statut()).isEqualTo(StatutInvitation.EN_ATTENTE);
        assertThat(emise.invitation().expireLe()).isEqualTo(MAINTENANT.plus(Duration.ofDays(7)));
        assertThat(emise.invitation().empreinte()).isEqualTo(Jeton.empreinte(emise.jeton())).isNotEqualTo(emise.jeton());
    }

    @Test
    @DisplayName("RG-OPE-41 : relancer remplace le jeton et repousse l'expiration ; annuler ; expirer")
    void rg_ope_41_cycle_de_vie() {
        Invitation invitation = Invitation.emettre(1, Email.de("marc@lafitte.fr", "t"), Role.AGENT, 5L, null, null,
                null, "Anne", MAINTENANT, Duration.ofDays(7), null).invitation();
        String empreinte = invitation.empreinte();

        String jeton = invitation.relancer(MAINTENANT.plus(Duration.ofDays(3)), Duration.ofDays(7));
        assertThat(invitation.empreinte()).isNotEqualTo(empreinte).isEqualTo(Jeton.empreinte(jeton));
        assertThat(invitation.expireLe()).isEqualTo(MAINTENANT.plus(Duration.ofDays(10)));

        assertThat(invitation.expirerSiEchue(MAINTENANT.plus(Duration.ofDays(9)))).isFalse();
        assertThat(invitation.expirerSiEchue(MAINTENANT.plus(Duration.ofDays(10)))).isTrue();
        assertThat(invitation.statut()).isEqualTo(StatutInvitation.EXPIREE);
        assertThat(invitation.fiche()).as("le contact dont elle partait reste désigné").contains(5L);
        assertThatThrownBy(invitation::annuler).extracting(PersonnesTest::regle).isEqualTo("RG-OPE-41");
    }

    @Test
    @DisplayName("RG-OPE-42 : seule une invitation en attente et non expirée s'accepte")
    void rg_ope_42_acceptable() {
        Invitation invitation = Invitation.emettre(1, Email.de("marc@lafitte.fr", "t"), Role.ADMIN, null,
                new IdentitePersonne(null, "Lafitte", "Marc"), null, null, "Anne", MAINTENANT, Duration.ofDays(7), null)
                .invitation();

        assertThatThrownBy(() -> invitation.verifierAcceptable(MAINTENANT.plus(Duration.ofDays(7))))
                .hasMessageContaining("expiré");
        invitation.accepter(42, MAINTENANT.plusSeconds(10));
        assertThat(invitation.statut()).isEqualTo(StatutInvitation.ACCEPTEE);
        assertThatThrownBy(() -> invitation.accepter(43, MAINTENANT.plusSeconds(20)))
                .hasMessageContaining("déjà été acceptée");
        assertThat(invitation.accepteeRecemmentPar(MAINTENANT.plus(Duration.ofMinutes(30)), Duration.ofHours(1)))
                .contains(42L);
        assertThat(invitation.accepteeRecemmentPar(MAINTENANT.plus(Duration.ofHours(2)), Duration.ofHours(1))).isEmpty();
    }

    @Test
    @DisplayName("RG-OPE-43 : changer le rôle, suspendre et réactiver un rattachement")
    void rg_ope_43_rattachement() {
        Rattachement rattachement = Rattachement.creer(1, 2, Role.ADMIN, 3, MAINTENANT);
        assertThat(rattachement.estAdminActif()).isTrue();

        rattachement.suspendre();
        assertThat(rattachement.estAdminActif()).isFalse();
        assertThatThrownBy(rattachement::suspendre).extracting(PersonnesTest::regle).isEqualTo("RG-OPE-43");
        rattachement.reactiver();
        rattachement.changerRole(Role.OBSERVATEUR);
        assertThat(rattachement.role()).isEqualTo(Role.OBSERVATEUR);
        assertThatThrownBy(() -> rattachement.changerRole(Role.OBSERVATEUR)).hasMessageContaining("déjà le rôle");
    }

    @Test
    @DisplayName("RG-OPE-47 : l'utilisateur modifie ses titre, nom et prénom")
    void rg_ope_47_profil() {
        Utilisateur utilisateur = Utilisateur.creer(IdentitePersonne.utilisateur(null, "Durand", "Jeanne"),
                Email.de("j@d.fr", "t"), 1);

        List<Changement> changements = utilisateur.modifierProfil(IdentitePersonne.utilisateur(10L, "Durand-Roy", "Jeanne"), 0L);

        assertThat(changements).extracting(Changement::champ).containsExactly(ChampJournal.TITRE, ChampJournal.NOM);
        assertThat(utilisateur.identite().nomComplet()).isEqualTo("Jeanne Durand-Roy");
    }

    @Test
    @DisplayName("RG-OPE-48 : une demande de changement d'e-mail à la fois, confirmée par un lien valable 24 heures")
    void rg_ope_48_changement_email() {
        Utilisateur utilisateur = Utilisateur.creer(IdentitePersonne.utilisateur(null, "Durand", "Jeanne"),
                Email.de("jeanne@durand.fr", "t"), 1);
        assertThatThrownBy(() -> utilisateur.demanderChangementEmail(Email.de("JEANNE@durand.fr", "t"), MAINTENANT,
                Duration.ofHours(24))).extracting(PersonnesTest::regle).isEqualTo("RG-OPE-48");

        String premier = utilisateur.demanderChangementEmail(Email.de("j1@durand.fr", "t"), MAINTENANT, Duration.ofHours(24));
        String second = utilisateur.demanderChangementEmail(Email.de("j2@durand.fr", "t"), MAINTENANT, Duration.ofHours(24));
        assertThat(utilisateur.changementEmail().orElseThrow().empreinte()).as("la nouvelle demande remplace l'ancienne")
                .isEqualTo(Jeton.empreinte(second)).isNotEqualTo(Jeton.empreinte(premier));

        assertThatThrownBy(() -> utilisateur.confirmerChangementEmail(MAINTENANT.plus(Duration.ofHours(24))))
                .hasMessageContaining("expiré");
        List<Changement> changements = utilisateur.confirmerChangementEmail(MAINTENANT.plus(Duration.ofHours(23)));
        assertThat(changements).containsExactly(new Changement(ChampJournal.EMAIL, "jeanne@durand.fr", "j2@durand.fr"));
        assertThat(utilisateur.email().valeur()).isEqualTo("j2@durand.fr");
        assertThat(utilisateur.changementEmail()).isEmpty();
    }

    @Test
    @DisplayName("RG-OPE-50 : bloquer exige un motif ; débloquer rétablit, sans toucher à l'opérateur actif")
    void rg_ope_50_blocage() {
        Utilisateur utilisateur = Utilisateur.creer(IdentitePersonne.utilisateur(null, "Roy", "Pierre"),
                Email.de("pierre@roy.fr", "t"), 4);

        assertThatThrownBy(() -> utilisateur.bloquer("  ")).extracting(PersonnesTest::regle).isEqualTo("RG-OPE-50");
        utilisateur.bloquer("Compte compromis");
        assertThat(utilisateur.estBloque()).isTrue();
        assertThatThrownBy(() -> utilisateur.bloquer("encore")).hasMessageContaining("déjà bloqué");
        utilisateur.debloquer();
        assertThat(utilisateur.estBloque()).isFalse();
        assertThat(utilisateur.operateurActif()).contains(4L);
    }

    @Test
    @DisplayName("RG-OPE-49 : l'opérateur actif est proposé à la première invitation acceptée, puis choisi par l'utilisateur")
    void rg_ope_49_operateur_actif() {
        Utilisateur utilisateur = Utilisateur.reconstituer(1L, IdentitePersonne.utilisateur(null, "Durand", "Jeanne"),
                Email.de("j@d.fr", "t"), StatutUtilisateur.ACTIF, null, null, 0);

        utilisateur.proposerOperateurActif(5);
        utilisateur.proposerOperateurActif(6);
        assertThat(utilisateur.operateurActif()).contains(5L);
        utilisateur.choisirOperateurActif(6L);
        assertThat(utilisateur.operateurActif()).contains(6L);
    }
}
