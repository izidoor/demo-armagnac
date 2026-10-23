package fr.bnia.boot.demo;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.declarations.application.DsmLigne;
import fr.bnia.declarations.application.GererDsm;
import fr.bnia.declarations.domain.partage.EtatDsm;
import fr.bnia.facturation.application.AnnoterPiece;
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
 * Chargeur de démonstration de la facturation (spec 08 §7.1, lots 1, 2, 5 et 6), après le personnel, les référentiels, les
 * opérateurs et les déclarations : relançable, et conforme à la spec (révisée le 2026-10-23 : ni brouillon ni DSM de
 * Gascogne à facturer ; Zeendoc selon le mois d'émission).
 */
class DemoFacturationIT extends AbstractIT {

    @Autowired ApplicationContext contexte;
    @Autowired GererDsm dsms;
    @Autowired ConsulterFactures factures;
    @Autowired AnnoterPiece notes;
    @Autowired ParametrerVendeur vendeur;

    @Test
    void parametre_le_vendeur_emet_des_factures_datees_sans_brouillon_et_peut_etre_relance() throws Exception {
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
        // Aucun brouillon au démarrage (décision du 2026-10-23).
        assertThat(factures.lister(lecteur, FiltrePieces.aucun()))
                .noneMatch(p -> p.etat() == EtatPiece.BROUILLON);
        long gascogne = operateurs.operateurParCode("N320001").orElseThrow().id();
        List<PieceLigne> pieces = factures.lister(lecteur, new FiltrePieces(Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.of(Long.toString(gascogne)), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty()));
        // Lot 5 : onze factures émises à des dates passées, une par mois, numérotées dans l'ordre des dates.
        List<PieceLigne> emises = pieces.stream().filter(p -> p.etat() == EtatPiece.EMISE)
                .sorted(Comparator.comparing(PieceLigne::dateEmission)).toList();
        assertThat(emises).hasSize(11).allSatisfy(p -> {
            assertThat(p.ttc()).isEqualByComparingTo("2152.90");
            assertThat(p.dateEmission()).isBefore(LocalDate.now());
        });
        assertThat(emises).extracting(p -> YearMonth.from(p.dateEmission())).doesNotHaveDuplicates();
        for (int i = 1; i < emises.size(); i++) {
            if (emises.get(i).dateEmission().getYear() == emises.get(i - 1).dateEmission().getYear()) {
                assertThat(emises.get(i).numero()).isGreaterThan(emises.get(i - 1).numero());
            }
        }
        // Lot 6 : les factures émises avant le mois courant sont transmises à Zeendoc, et payées sauf celle de
        // Gascogne émise il y a deux mois ; celles du mois courant restent à transmettre.
        LocalDate debutDuMois = LocalDate.now().withDayOfMonth(1);
        YearMonth impayee = YearMonth.now().minusMonths(2);
        assertThat(emises).filteredOn(p -> YearMonth.from(p.dateEmission()).equals(impayee)).singleElement()
                .satisfies(p -> {
                    assertThat(p.zeendoc().etat()).isEqualTo(fr.bnia.facturation.domain.EtatZeendoc.TRANSMISE);
                    assertThat(p.zeendoc().payeeLe()).isNull();
                });
        assertThat(factures.lister(lecteur, FiltrePieces.aucun())).filteredOn(p -> p.etat() == EtatPiece.EMISE)
                .allSatisfy(p -> {
                    if (!p.dateEmission().isBefore(debutDuMois)) {
                        assertThat(p.zeendoc()).isNull();
                    } else {
                        assertThat(p.zeendoc()).isNotNull();
                    }
                })
                .filteredOn(p -> p.zeendoc() != null && p.zeendoc().etat() == fr.bnia.facturation.domain.EtatZeendoc.PAYEE)
                .allSatisfy(p -> assertThat(p.zeendoc().payeeLe()).isAfter(p.dateEmission())
                        .isBefore(LocalDate.now()));
        assertThat(factures.lister(lecteur, FiltrePieces.aucun()))
                .filteredOn(p -> p.zeendoc() != null && p.zeendoc().payeeLe() == null)
                .as("transmises impayées : celle de Gascogne, et celle de la veille d'un 1er du mois")
                .allSatisfy(p -> assertThat(p.client().identifiant().equals(Long.toString(gascogne))
                        || p.dateEmission().equals(LocalDate.now().minusDays(1))).isTrue());
        List<DsmLigne> validees = dsms.lister(anne, gascogne).stream().map(GererDsm.DsmDeLOperateur::dsm)
                .filter(d -> d.etat() == EtatDsm.VALIDEE).toList();
        // Lot 2 : onze DSM validées, toutes facturées (une note sur la première facture).
        assertThat(validees).hasSize(11).allMatch(DsmLigne::facturee);
        assertThat(notes.lister(lecteur, emises.getFirst().id())).singleElement()
                .satisfies(n -> assertThat(n.auteur().nom()).isEqualTo("Anne Lamarque"));

        long condom = operateurs.operateurParCode("000001").orElseThrow().id();
        assertThat(dsms.lister(anne, condom)).filteredOn(d -> d.dsm().etat() == EtatDsm.VALIDEE)
                .allSatisfy(d -> assertThat(d.dsm().facturee()).isTrue());
        // Volume (lot 5) : chaque négociant ajouté a ses DSM mensuelles de l'année, jusqu'au mois dernier (sauf celui
        // en retard) ; chaque producteur, celle de sa dernière année déclarative close ; toutes facturées, émises
        // avant aujourd'hui.
        for (OperateursFacturesDemo.Operateur o : OperateursFacturesDemo.OPERATEURS) {
            long id = operateurs.operateurParCode(o.code()).orElseThrow().id();
            List<DsmLigne> leurs = dsms.lister(anne, id).stream().map(GererDsm.DsmDeLOperateur::dsm)
                    .filter(d -> d.etat() == EtatDsm.VALIDEE).toList();
            assertThat(leurs).as(o.raisonSociale())
                    .hasSize(OperateursFacturesDemo.dsmValidees(o, LocalDate.now()))
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
