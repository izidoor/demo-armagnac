package fr.bnia.boot.demo;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.operateurs.application.GererOperateurs;
import fr.bnia.operateurs.application.GererPresentation;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.EtatConnexion;
import fr.bnia.operateurs.domain.partage.NiveauDiffusion;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import fr.bnia.operateurs.domain.partage.PersonneVue;
import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.support.AbstractIT;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;

/**
 * Chargeurs de démonstration du personnel du BNIA et des opérateurs, sur les référentiels de
 * démonstration : relançables, et conformes à la spec 02 (§7.1).
 */
class DemoOperateursIT extends AbstractIT {

    @Autowired GererOperateurs gerer;
    @Autowired GererPresentation presentation;
    @Autowired ApplicationContext contexte;

    @Test
    void charge_le_personnel_et_les_operateurs_et_peut_etre_relance() {
        var fabrique = contexte.getAutowireCapableBeanFactory();
        var personnel = fabrique.createBean(DemoPersonnelBnia.class);
        var referentiels = fabrique.createBean(DemoReferentiels.class);
        var demo = fabrique.createBean(DemoOperateurs.class);
        for (int i = 0; i < 2; i++) {
            personnel.run(new DefaultApplicationArguments());
            referentiels.run(new DefaultApplicationArguments());
            demo.run(new DefaultApplicationArguments());
        }

        Acteur anne = new Acteur(DemoPersonnelBnia.anne(operateurs));
        assertThat(operateurs.droits(anne.utilisateur(), operateurs.operateurBnia()).local()).contains(Role.ADMIN);

        OperateurVue condom = operateurs.operateurParCode("000001").orElseThrow();
        assertThat(condom.niveauDiffusion()).isEqualTo(NiveauDiffusion.PARTIEL);
        assertThat(operateurs.personnes(condom.id())).extracting(PersonneVue::nomComplet)
                .containsExactly("Jeanne Durand", "Sophie Martin", "Paul Petit", "Pierre Roy");
        long jeanne = operateurs.utilisateurParEmail("admin@producteur.fr").orElseThrow();
        long eauze = operateurs.operateurParCode("000002").orElseThrow().id();
        assertThat(operateurs.droits(jeanne, condom.id()).local()).contains(Role.ADMIN);
        assertThat(operateurs.droits(jeanne, eauze).local()).contains(Role.ADMIN);
        long pierre = operateurs.utilisateurParEmail("observateur@producteur.fr").orElseThrow();
        assertThat(operateurs.droits(pierre, condom.id()).local()).contains(Role.OBSERVATEUR);
        assertThat(operateurs.droits(pierre, eauze).aucunAcces()).isTrue();
        assertThat(operateurs.etatConnexion(pierre)).isEqualTo(EtatConnexion.AUTORISE);
        long negociant = operateurs.operateurParCode("N320001").orElseThrow().id();
        for (var compte : Map.of("admin", Role.ADMIN, "agent", Role.AGENT, "observateur", Role.OBSERVATEUR).entrySet()) {
            long bnia = operateurs.utilisateurParEmail(compte.getKey() + "@bnia.fr").orElseThrow();
            assertThat(operateurs.droits(bnia, operateurs.operateurBnia()).local()).contains(compte.getValue());
            long producteur = operateurs.utilisateurParEmail(compte.getKey() + "@producteur.fr").orElseThrow();
            assertThat(operateurs.droits(producteur, condom.id()).local()).contains(compte.getValue());
            long negoce = operateurs.utilisateurParEmail(compte.getKey() + "@negociant.fr").orElseThrow();
            assertThat(operateurs.droits(negoce, negociant).local()).contains(compte.getValue());
        }
        assertThat(operateurs.utilisateurParEmail("marc.lafitte@distillerie-lafitte.example")).isEmpty();

        assertThat(operateurs.operateurParCode("N400001")).isPresent();
        assertThat(operateurs.operateurParCode("000010").orElseThrow().periodicite()).isEmpty();
        assertThat(operateurs.operateurParCode("000013").orElseThrow().actif()).isFalse();
        assertThat(gerer.fiche(anne, operateurs.operateurParCode("000012").orElseThrow().id()).champsManquants())
                .contains("adresse", "région");
        assertThat(gerer.fiche(anne, condom.id()).champsManquants()).isEmpty();
        assertThat(presentation.consulter(anne, condom.id()).medailles()).hasSize(2);

        var session = anonyme();
        session.post().uri("/api/authentification/connexion")
                .body(Map.of("email", "admin@producteur.fr", "motDePasse", "demo-armagnac"))
                .exchange().expectStatus().isOk();
        session.post().uri("/api/authentification/code").body(Map.of("code", "000000", "faireConfiance", false))
                .exchange().expectStatus().isOk();
        session.get().uri("/api/moi").exchange()
                .expectBody().jsonPath("$.operateurOuvert").isEqualTo(condom.id());
        session.get().uri("/api/utilisateurs").exchange().expectStatus().isEqualTo(HttpStatus.FORBIDDEN);
    }
}
