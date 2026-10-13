package fr.bnia.boot.demo;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.application.CommenterDsm;
import fr.bnia.declarations.application.DsmLigne;
import fr.bnia.declarations.application.GererDsm;
import fr.bnia.declarations.application.GererPointDeDepart;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.Commentaire;
import fr.bnia.declarations.domain.EtatDsm;
import fr.bnia.declarations.domain.LigneDsm;
import fr.bnia.declarations.domain.TypeAnomalie;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.support.AbstractIT;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ApplicationContext;

/**
 * Chargeur de démonstration des déclarations (spec 05, §7.1, lots 1 et 2), après le personnel, les
 * référentiels et les opérateurs : relançable, et conforme à la spec.
 */
class DemoDeclarationsIT extends AbstractIT {

    @Autowired GererPointDeDepart points;
    @Autowired GererDsm dsms;
    @Autowired CommenterDsm commentaires;
    @Autowired ApplicationContext contexte;

    private Acteur anne;

    private long id(String code) {
        return operateurs.operateurParCode(code).orElseThrow().id();
    }

    /** La seule DSM de l'opérateur, dans cet état, qui commence à cette date. */
    private DsmLigne seule(String code, EtatDsm etat, LocalDate debut) {
        List<DsmLigne> declarations = dsms.lister(anne, id(code));
        assertThat(declarations).as(code).singleElement().satisfies(d -> {
            assertThat(d.etat()).isEqualTo(etat);
            assertThat(d.periode().debut()).isEqualTo(debut);
        });
        return declarations.getFirst();
    }

    @Test
    void charge_le_cycle_des_negociants_et_la_reprise_du_producteur_et_peut_etre_relance() {
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
        anne = new Acteur(DemoPersonnelBnia.anne(operateurs));
        LocalDate mois = LocalDate.now().withDayOfMonth(1);

        long gascogne = id("N320001");
        assertThat(points.consulter(anne, gascogne).orElseThrow().point().lignes()).hasSize(6);
        var envoyee = dsms.consulter(anne, gascogne, seule("N320001", EtatDsm.ENVOYEE, mois).id());
        assertThat(envoyee.anomalies()).isEmpty();
        List<LigneDsm> lignes = envoyee.dsm().lignes();
        assertThat(somme(lignes, ColonneDsm.C14, ColonneDsm.C17, ColonneDsm.C18, ColonneDsm.C19, ColonneDsm.C21))
                .isEqualTo("839.11");
        assertThat(somme(lignes, ColonneDsm.C22, ColonneDsm.C23, ColonneDsm.C24)).isEqualTo("2254.15");
        assertThat(commentaires.lister(anne, gascogne, envoyee.dsm().id())).extracting(Commentaire::interne)
                .containsExactly(false, true);

        long larressingle = id("N320002");
        DsmLigne complements = seule("N320002", EtatDsm.COMPLEMENTS_DEMANDES, mois);
        assertThat(commentaires.lister(anne, larressingle, complements.id())).singleElement()
                .satisfies(c -> assertThat(c.texte()).isEqualTo("DAE manquants pour l'export"));

        var brouillon = dsms.consulter(anne, id("N400001"), seule("N400001", EtatDsm.BROUILLON, mois).id());
        assertThat(brouillon.anomalies()).extracting(a -> a.type())
                .containsExactly(TypeAnomalie.ASSEMBLAGES_DESEQUILIBRES);

        LocalDate aujourdhui = LocalDate.now();
        int annee = aujourdhui.getMonthValue() >= 4 ? aujourdhui.getYear() : aujourdhui.getYear() - 1;
        seule("000001", EtatDsm.VALIDEE, LocalDate.of(annee - 1, 4, 1));
        assertThat(dsms.situation(anne, id("000001")).initiable()).as("la DSM suivante est à initier").isTrue();

        assertThat(points.consulter(anne, id("000002"))).isEmpty();
    }

    private static String somme(List<LigneDsm> lignes, ColonneDsm... colonnes) {
        return lignes.stream()
                .flatMap(l -> Stream.of(colonnes).map(l::volume))
                .reduce(VolumeAlcoolPur.ZERO, VolumeAlcoolPur::plus)
                .toString();
    }
}
