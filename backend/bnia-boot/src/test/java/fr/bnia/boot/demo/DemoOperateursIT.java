package fr.bnia.boot.demo;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.operateurs.application.GererContacts;
import fr.bnia.operateurs.application.GererOperateurs;
import fr.bnia.operateurs.application.GererPresentation;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.ContactVue;
import fr.bnia.operateurs.domain.partage.NiveauDiffusion;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import fr.bnia.support.AbstractIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ApplicationContext;

/**
 * Chargeur de démonstration des opérateurs, sur les référentiels de démonstration : relançable, et
 * conforme à la spec 02 (§7.1).
 */
class DemoOperateursIT extends AbstractIT {

    @Autowired GererOperateurs operateurs;
    @Autowired GererContacts contacts;
    @Autowired GererPresentation presentation;
    @Autowired OperateursLecture lecture;
    @Autowired ReferentielsLecture referentiels;
    @Autowired ApplicationContext contexte;

    @Test
    void charge_les_operateurs_et_peut_etre_relance() {
        var demoReferentiels = contexte.getAutowireCapableBeanFactory().createBean(DemoReferentiels.class);
        demoReferentiels.run(new DefaultApplicationArguments());
        var demo = new DemoOperateurs(operateurs, contacts, presentation, lecture, referentiels);

        demo.run(new DefaultApplicationArguments());
        demo.run(new DefaultApplicationArguments());

        OperateurVue condom = lecture.operateurParCode("000001").orElseThrow();
        assertThat(condom.niveauDiffusion()).isEqualTo(NiveauDiffusion.PARTIEL);
        assertThat(lecture.contacts(condom.id())).extracting(ContactVue::nomComplet)
                .containsExactly("Jeanne Durand", "Paul Petit");
        assertThat(lecture.contactsAvecAccesParEmail("jeanne.durand@maison-durand.example"))
                .extracting(ContactVue::operateur)
                .containsExactlyInAnyOrder(condom.id(), lecture.operateurParCode("000002").orElseThrow().id());
        assertThat(lecture.operateurParCode("N400001")).isPresent();
        assertThat(lecture.operateurParCode("000010").orElseThrow().periodicite()).isEmpty();
        assertThat(lecture.operateurParCode("000013").orElseThrow().actif()).isFalse();
        assertThat(operateurs.fiche(lecture.operateurParCode("000012").orElseThrow().id()).champsManquants())
                .contains("adresse", "région");
        assertThat(operateurs.fiche(condom.id()).champsManquants()).isEmpty();
        assertThat(presentation.consulter(condom.id()).medailles()).hasSize(2);
    }
}
