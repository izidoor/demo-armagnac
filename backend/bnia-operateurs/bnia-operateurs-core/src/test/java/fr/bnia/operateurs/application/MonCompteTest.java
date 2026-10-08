package fr.bnia.operateurs.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.ActionJournal;
import fr.bnia.operateurs.domain.Rattachement;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.EmailUtilisateurModifie;
import fr.bnia.operateurs.domain.partage.Role;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MonCompteTest {

    private final Memoire memoire = new Memoire();
    private final MonCompte monCompte = memoire.monCompte();
    private final long condom = memoire.operateur("000001", "Maison Durand, Condom");
    private final long eauze = memoire.operateur("000002", "Maison Durand, Eauze");
    private final Acteur jeanne = memoire.membre(condom, Role.ADMIN, "Jeanne", "Durand");
    private final Acteur sophie = memoire.membre(condom, Role.AGENT, "Sophie", "Martin");

    {
        memoire.membre(eauze, Role.ADMIN, "Jeanne", "Durand");
    }

    @Test
    @DisplayName("RG-OPE-34, 47 et 51 : l'utilisateur seul modifie son profil, visible dans tous ses opérateurs")
    void rg_ope_47_profil() {
        monCompte.modifierProfil(jeanne, new SaisieProfil(Memoire.TITRE, "Durand-Roy", "Jeanne", 0L));

        assertThat(memoire.utilisateurs.parId(jeanne.utilisateur()).orElseThrow().identite().nom()).isEqualTo("Durand-Roy");
        assertThat(memoire.journal.derniere().action()).isEqualTo(ActionJournal.PROFIL_MODIFIE);
        assertThatThrownBy(() -> monCompte.modifierProfil(jeanne, new SaisieProfil(null, "Durand", "Jeanne", 0L)))
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-51");
    }

    @Test
    @DisplayName("RG-OPE-48 : changer d'e-mail passe par un lien envoyé au nouvel e-mail ; l'identifiant de connexion suit")
    void rg_ope_48_changement_email() {
        assertThatThrownBy(() -> monCompte.demanderChangementEmail(jeanne, "sophie.martin@exemple.fr"))
                .isInstanceOf(ConflitUnicite.class).hasMessageContaining("autre compte");

        monCompte.demanderChangementEmail(jeanne, "jeanne@maison-durand.fr");
        assertThat(memoire.utilisateurs.parId(jeanne.utilisateur()).orElseThrow().email().valeur())
                .as("rien ne change avant la confirmation").isEqualTo("jeanne.durand@exemple.fr");
        String jeton = memoire.courriels.jeton("jeanne@maison-durand.fr");
        monCompte.confirmerChangementEmail(jeton);

        assertThat(memoire.utilisateurs.parId(jeanne.utilisateur()).orElseThrow().email().valeur())
                .isEqualTo("jeanne@maison-durand.fr");
        assertThat(memoire.evenements.publies)
                .containsExactly(new EmailUtilisateurModifie(jeanne.utilisateur(), "jeanne@maison-durand.fr"));
        assertThat(memoire.courriels.envoyes.getLast().destinataire()).as("l'ancien e-mail est prévenu")
                .isEqualTo("jeanne.durand@exemple.fr");
        assertThatThrownBy(() -> monCompte.confirmerChangementEmail(jeton)).isInstanceOf(ElementIntrouvable.class);
    }

    @Test
    @DisplayName("RG-OPE-48 : la confirmation est refusée si l'e-mail a été pris entre-temps, ou si le lien a expiré")
    void rg_ope_48_refus_a_la_confirmation() {
        monCompte.demanderChangementEmail(jeanne, "partage@durand.fr");
        monCompte.demanderChangementEmail(sophie, "partage@durand.fr");
        monCompte.confirmerChangementEmail(memoire.courriels.jeton("partage@durand.fr"));

        monCompte.demanderChangementEmail(jeanne, "autre@durand.fr");
        memoire.horloge.avancer(Duration.ofHours(25));
        assertThatThrownBy(() -> monCompte.confirmerChangementEmail(memoire.courriels.jeton("autre@durand.fr")))
                .hasMessageContaining("expiré");
    }

    @Test
    @DisplayName("RG-OPE-49 : l'application s'ouvre sur l'opérateur actif s'il est utilisable ; l'utilisateur en change à tout moment")
    void rg_ope_49_operateur_actif() {
        assertThat(monCompte.moi(jeanne).operateurOuvert()).contains(condom);
        assertThat(monCompte.moi(jeanne).rattachements()).hasSize(2);

        assertThat(monCompte.choisirOperateurActif(jeanne, eauze).operateurOuvert()).contains(eauze);
        assertThatThrownBy(() -> monCompte.choisirOperateurActif(jeanne, memoire.bnia))
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-49");

        Rattachement aEauze = memoire.rattachementDe(jeanne, eauze);
        aEauze.suspendre();
        memoire.rattachements.mettreAJour(aEauze);
        assertThat(monCompte.moi(jeanne).operateurOuvert()).as("rattachement suspendu : il choisit").isEmpty();
    }
}
