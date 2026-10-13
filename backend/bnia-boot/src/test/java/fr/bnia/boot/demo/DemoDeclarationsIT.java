package fr.bnia.boot.demo;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.declarations.application.DsmLigne;
import fr.bnia.declarations.application.GererDsm;
import fr.bnia.declarations.application.GererPointDeDepart;
import fr.bnia.declarations.domain.EtatDsm;
import fr.bnia.declarations.domain.TypeAnomalie;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.support.AbstractIT;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ApplicationContext;

/**
 * Chargeur de démonstration des déclarations (spec 05, §7.1, lot 1), après le personnel, les référentiels
 * et les opérateurs : relançable, et conforme à la spec.
 */
class DemoDeclarationsIT extends AbstractIT {

    @Autowired GererPointDeDepart points;
    @Autowired GererDsm dsms;
    @Autowired ApplicationContext contexte;

    @Test
    void charge_le_point_de_depart_et_la_dsm_du_negociant_et_peut_etre_relance() {
        var fabrique = contexte.getAutowireCapableBeanFactory();
        var personnel = fabrique.createBean(DemoPersonnelBnia.class);
        var referentiels = fabrique.createBean(DemoReferentiels.class);
        var demoOperateurs = fabrique.createBean(DemoOperateurs.class);
        var demo = fabrique.createBean(DemoDeclarations.class);
        for (int i = 0; i < 2; i++) {
            personnel.run(new DefaultApplicationArguments());
            referentiels.run(new DefaultApplicationArguments());
            demoOperateurs.run(new DefaultApplicationArguments());
            demo.run(new DefaultApplicationArguments());
        }

        Acteur anne = new Acteur(DemoPersonnelBnia.anne(operateurs));
        long negociant = operateurs.operateurParCode("N320001").orElseThrow().id();
        var point = points.consulter(anne, negociant).orElseThrow().point();
        assertThat(point.debut()).isEqualTo(LocalDate.now().withDayOfMonth(1).minusMonths(4));
        assertThat(point.lignes()).hasSize(6);

        List<DsmLigne> declarations = dsms.lister(anne, negociant);
        assertThat(declarations).singleElement().satisfies(d -> {
            assertThat(d.etat()).isEqualTo(EtatDsm.BROUILLON);
            assertThat(d.periode().debut()).isEqualTo(point.debut());
        });
        var vue = dsms.consulter(anne, negociant, declarations.getFirst().id());
        assertThat(vue.dsm().lignes()).hasSize(6);
        assertThat(vue.anomalies()).extracting(a -> a.type()).containsExactly(TypeAnomalie.ASSEMBLAGES_DESEQUILIBRES);

        long eauze = operateurs.operateurParCode("000002").orElseThrow().id();
        assertThat(points.consulter(anne, eauze)).isEmpty();
    }
}
