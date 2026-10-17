package fr.bnia.boot.demo;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.declarations.application.DsmLigne;
import fr.bnia.declarations.application.GererDsm;
import fr.bnia.declarations.domain.partage.EtatDsm;
import fr.bnia.facturation.application.ConsulterFactures;
import fr.bnia.facturation.application.FiltrePieces;
import fr.bnia.facturation.application.ParametrerVendeur;
import fr.bnia.facturation.application.PieceLigne;
import fr.bnia.facturation.domain.EtatPiece;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.support.AbstractIT;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ApplicationContext;

/**
 * Chargeur de démonstration de la facturation (spec 08 §7.1, lots 1, 2 et 5), après le personnel, les référentiels, les
 * opérateurs et les déclarations : relançable, et conforme à la spec.
 */
class DemoFacturationIT extends AbstractIT {

    @Autowired ApplicationContext contexte;
    @Autowired GererDsm dsms;
    @Autowired ConsulterFactures factures;
    @Autowired ParametrerVendeur vendeur;

    @Test
    void parametre_le_vendeur_emet_des_factures_datees_et_laisse_un_brouillon_et_peut_etre_relance() throws Exception {
        var fabrique = contexte.getAutowireCapableBeanFactory();
        var chargeurs = List.of(fabrique.createBean(DemoPersonnelBnia.class), fabrique.createBean(DemoReferentiels.class),
                fabrique.createBean(DemoOperateurs.class), fabrique.createBean(DemoDeclarations.class),
                fabrique.createBean(DemoFacturation.class));
        for (int i = 0; i < 2; i++) {
            for (var chargeur : chargeurs) {
                ((org.springframework.boot.ApplicationRunner) chargeur).run(new DefaultApplicationArguments());
            }
        }
        Acteur anne = new Acteur(DemoPersonnelBnia.anne(operateurs));
        var lecteur = new fr.bnia.facturation.api.Acteur(anne.utilisateur());

        assertThat(vendeur.consulter(lecteur).orElseThrow().siret()).isEqualTo("77699995500029");
        long gascogne = operateurs.operateurParCode("N320001").orElseThrow().id();
        List<PieceLigne> pieces = factures.lister(lecteur, new FiltrePieces(Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.of(Long.toString(gascogne)), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty()));
        // Lot 5 : huit factures émises à des dates passées, une par mois, numérotées dans l'ordre des dates ; un
        // brouillon.
        List<PieceLigne> emises = pieces.stream().filter(p -> p.etat() == EtatPiece.EMISE)
                .sorted(Comparator.comparing(PieceLigne::dateEmission)).toList();
        assertThat(emises).hasSize(DemoFacturation.EMISES_GASCOGNE).allSatisfy(p -> {
            assertThat(p.ttc()).isEqualByComparingTo("2152.90");
            assertThat(p.dateEmission()).isBefore(LocalDate.now());
        });
        assertThat(emises).extracting(p -> YearMonth.from(p.dateEmission())).doesNotHaveDuplicates();
        for (int i = 1; i < emises.size(); i++) {
            if (emises.get(i).dateEmission().getYear() == emises.get(i - 1).dateEmission().getYear()) {
                assertThat(emises.get(i).numero()).isGreaterThan(emises.get(i - 1).numero());
            }
        }
        assertThat(pieces).filteredOn(p -> p.etat() == EtatPiece.BROUILLON).hasSize(1);
        List<DsmLigne> validees = dsms.lister(anne, gascogne).stream().map(GererDsm.DsmDeLOperateur::dsm)
                .filter(d -> d.etat() == EtatDsm.VALIDEE).toList();
        // Lot 2 : onze DSM validées (de la plus récente à la plus ancienne) : huit facturées (une note sur la
        // première), un brouillon, deux à facturer en lot.
        assertThat(validees).hasSize(11);
        assertThat(validees.subList(3, 11)).allMatch(DsmLigne::facturee);
        assertThat(validees.get(2).facturationDemandee()).isTrue();
        assertThat(validees.subList(0, 2)).noneMatch(d -> d.facturee() || d.facturationDemandee());
        assertThat(factures.journal(lecteur, emises.getFirst().id())).extracting(e -> e.evenement())
                .contains(fr.bnia.facturation.domain.EvenementJournal.NOTE);

        long condom = operateurs.operateurParCode("000001").orElseThrow().id();
        assertThat(dsms.lister(anne, condom)).filteredOn(d -> d.dsm().etat() == EtatDsm.VALIDEE)
                .allSatisfy(d -> assertThat(d.dsm().facturee()).isTrue());
        // Volume (lot 5) : chaque négociant ajouté a ses DSM mensuelles de l'année, jusqu'au mois dernier ; chaque
        // producteur, celle de sa dernière année déclarative close ; toutes facturées, émises avant aujourd'hui.
        for (OperateursFacturesDemo.Operateur o : OperateursFacturesDemo.OPERATEURS) {
            long id = operateurs.operateurParCode(o.code()).orElseThrow().id();
            List<DsmLigne> leurs = dsms.lister(anne, id).stream().map(GererDsm.DsmDeLOperateur::dsm)
                    .filter(d -> d.etat() == EtatDsm.VALIDEE).toList();
            assertThat(leurs).as(o.raisonSociale())
                    .hasSize(o.negociant() ? LocalDate.now().getMonthValue() - 1 : 1)
                    .allMatch(DsmLigne::facturee);
            assertThat(factures.lister(lecteur, new FiltrePieces(Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.of(Long.toString(id)), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty()))).as(o.raisonSociale()).hasSize(leurs.size())
                    .allSatisfy(p -> assertThat(p.dateEmission()).isBefore(LocalDate.now()));
        }

        long laubade = operateurs.operateurParCode("000003").orElseThrow().id();
        assertThat(dsms.lister(anne, laubade)).allSatisfy(d -> {
            assertThat(d.dsm().facturee()).isFalse();
            assertThat(d.dsm().facturationDemandee()).isFalse();
        });
    }
}
