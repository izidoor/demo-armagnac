package fr.bnia.operateurs.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.ActionContact;
import fr.bnia.operateurs.domain.Canal;
import fr.bnia.operateurs.domain.Changement;
import fr.bnia.operateurs.domain.ChampContact;
import fr.bnia.operateurs.domain.partage.AccesPortailAccorde;
import fr.bnia.operateurs.domain.partage.ModulePortail;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import fr.bnia.referentiels.domain.partage.ValeurVue;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GererContactsTest {

    private static final Instant MAINTENANT = Instant.parse("2026-10-06T09:30:00Z");
    private static final long COMPTA = 1;
    private static final long ANCIEN = 2;

    private final OperateursEnMemoire memoire = new OperateursEnMemoire();
    private final JournalEnMemoire journal = new JournalEnMemoire();
    private final EvenementsCaptures evenements = new EvenementsCaptures();
    private final ReferentielsLecture referentiels = mock(ReferentielsLecture.class);
    private final Clock horloge = Clock.fixed(MAINTENANT, ZoneOffset.UTC);
    private final GererOperateurs operateurs = new GererOperateurs(memoire, memoire, memoire, referentiels,
            mock(OperateursLecture.class), journal, evenements, horloge);
    private final GererContacts gerer = new GererContacts(memoire, memoire, journal, evenements, referentiels, horloge);

    private long condom;
    private long eauze;

    @BeforeEach
    void maisonDurand() {
        when(referentiels.centreInteret(COMPTA)).thenReturn(Optional.of(new ValeurVue(COMPTA, "COMPTA", "Comptabilité", true)));
        when(referentiels.centreInteret(ANCIEN)).thenReturn(Optional.of(new ValeurVue(ANCIEN, "OLD", "Ancien", false)));
        condom = operateurs.creer(GererOperateursTest.saisie(TypeOperateur.PRODUCTEUR, "000012", "Chai de Condom", "32100", null)).id();
        eauze = operateurs.creer(GererOperateursTest.saisie(TypeOperateur.PRODUCTEUR, "000013", "Chai d'Eauze", "32800", null)).id();
    }

    private static SaisieContact durand(String email, Set<Long> centres) {
        return new SaisieContact(null, "Durand", "Jeanne", "Comptable", centres, null, null, email, null, null, null,
                null, null);
    }

    @Test
    @DisplayName("RG-OPE-21 : un centre d'intérêt choisi doit être actif")
    void rg_ope_21_centre_interet_actif() {
        assertThatThrownBy(() -> gerer.creer(condom, durand(null, Set.of(ANCIEN)), Canal.BACK_OFFICE))
                .isInstanceOf(RegleEnfreinte.class).hasMessageContaining("Ancien")
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-21");
    }

    @Test
    @DisplayName("RG-OPE-29 : accorder l'accès publie « accès portail accordé »")
    void rg_ope_29_evenement_publie() {
        long contact = gerer.creer(condom, durand("jeanne.durand@exemple.fr", Set.of()), Canal.BACK_OFFICE).id();

        gerer.modifierDroits(condom, contact, new SaisieDroits(true, true, Set.of()), Canal.BACK_OFFICE);

        assertThat(evenements.publies).containsExactly(new AccesPortailAccorde(condom, contact, "jeanne.durand@exemple.fr"));
        assertThat(gerer.consulter(condom, contact).contact().droits().modules()).hasSize(ModulePortail.values().length);
    }

    @Test
    @DisplayName("RG-OPE-34 : journal daté, signé, avec les libellés des référentiels")
    void rg_ope_34_journal() {
        long contact = gerer.creer(condom, durand(null, Set.of()), Canal.BACK_OFFICE).id();
        gerer.modifier(condom, contact, durand(null, Set.of(COMPTA)), Canal.PORTAIL);
        gerer.supprimer(condom, contact, Canal.BACK_OFFICE);

        List<LigneJournal> lignes = gerer.journal(condom, contact);

        assertThat(lignes).extracting(LigneJournal::action)
                .containsExactlyInAnyOrder(ActionContact.CREATION, ActionContact.MODIFICATION, ActionContact.SUPPRESSION);
        assertThat(lignes).allMatch(l -> l.date().equals(MAINTENANT) && l.nomContact().equals("Jeanne Durand"));
        LigneJournal modification = lignes.stream().filter(l -> l.action() == ActionContact.MODIFICATION).findFirst().orElseThrow();
        assertThat(modification.auteur()).isEqualTo("Portail");
        assertThat(modification.changements()).containsExactly(new Changement(ChampContact.CENTRES_INTERET, null, "Comptabilité"));
    }

    @Test
    @DisplayName("RG-OPE-32 : « compte créé » verrouille l'e-mail dans tous les opérateurs ; « identifiant modifié » le reporte")
    void rg_ope_32_evenements_de_acces() {
        long aCondom = gerer.creer(condom, durand("jeanne.durand@exemple.fr", Set.of()), Canal.BACK_OFFICE).id();
        long aEauze = gerer.creer(eauze, durand("Jeanne.Durand@exemple.fr", Set.of()), Canal.BACK_OFFICE).id();

        gerer.verrouillerEmail("jeanne.durand@exemple.fr");

        assertThat(gerer.consulter(condom, aCondom).contact().emailVerrouille()).isTrue();
        assertThat(gerer.consulter(eauze, aEauze).contact().emailVerrouille()).isTrue();
        assertThatThrownBy(() -> gerer.modifier(condom, aCondom, durand("autre@exemple.fr", Set.of()), Canal.BACK_OFFICE))
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-32");

        gerer.reporterIdentifiant("jeanne.durand@exemple.fr", "jeanne@durand.fr");

        assertThat(gerer.consulter(eauze, aEauze).contact().identite().email().valeur()).isEqualTo("jeanne@durand.fr");
        assertThat(gerer.journal(eauze, aEauze)).anyMatch(l -> l.auteur().equals("Mon compte"));
    }

    @Test
    @DisplayName("RG-OPE-35 : au portail, les contacts d'un opérateur inactif ne se gèrent plus")
    void rg_ope_35_operateur_inactif() {
        operateurs.desactiver(condom);

        assertThatThrownBy(() -> gerer.creer(condom, durand(null, Set.of()), Canal.PORTAIL))
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-35");
        assertThat(gerer.creer(condom, durand(null, Set.of()), Canal.BACK_OFFICE).estActif()).isTrue();
    }

    @Test
    @DisplayName("RG-OPE-23 : un destinataire des factures est soit un contact, soit une adresse")
    void rg_ope_23_designations() {
        long contact = gerer.creer(condom, durand("jeanne.durand@exemple.fr", Set.of()), Canal.BACK_OFFICE).id();

        assertThatThrownBy(() -> gerer.designer(condom, new SaisieDesignations(null, contact, "compta@exemple.fr"), Canal.BACK_OFFICE))
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-23");
        assertThatThrownBy(() -> gerer.designer(condom, new SaisieDesignations(null, null, "compta@"), Canal.BACK_OFFICE))
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-24");

        var designe = gerer.designer(condom, new SaisieDesignations(contact, contact, null), Canal.BACK_OFFICE);
        assertThat(designe.emailFacturation().orElseThrow().valeur()).isEqualTo("jeanne.durand@exemple.fr");
        assertThat(gerer.consulter(condom, contact).contactPrincipal()).isTrue();
    }
}
