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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ApplicationContext;

/**
 * Chargeur de démonstration de la facturation (spec 08 §7.1, lot 1), après le personnel, les référentiels, les
 * opérateurs et les déclarations : relançable, et conforme à la spec.
 */
class DemoFacturationIT extends AbstractIT {

    @Autowired ApplicationContext contexte;
    @Autowired GererDsm dsms;
    @Autowired ConsulterFactures factures;
    @Autowired ParametrerVendeur vendeur;

    @Test
    void parametre_le_vendeur_emet_une_facture_et_laisse_un_brouillon_et_peut_etre_relance() throws Exception {
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
        assertThat(pieces).singleElement().satisfies(p -> {
            assertThat(p.etat()).isEqualTo(EtatPiece.EMISE);
            assertThat(p.ttc()).isEqualByComparingTo("2152.90");
        });
        List<DsmLigne> validees = dsms.lister(anne, gascogne).stream().map(GererDsm.DsmDeLOperateur::dsm)
                .filter(d -> d.etat() == EtatDsm.VALIDEE).toList();
        // Lot 2 : onze DSM validées, la plus ancienne facturée (avec une note), dix à facturer en lot.
        assertThat(validees).hasSize(11);
        assertThat(validees.getLast().facturee()).isTrue();
        assertThat(validees.subList(0, 10)).noneMatch(DsmLigne::facturee);
        assertThat(factures.journal(lecteur, pieces.getFirst().id())).extracting(e -> e.evenement())
                .contains(fr.bnia.facturation.domain.EvenementJournal.NOTE);

        long condom = operateurs.operateurParCode("000001").orElseThrow().id();
        assertThat(dsms.lister(anne, condom)).extracting(d -> d.dsm().facturationDemandee()).contains(true);
        long laubade = operateurs.operateurParCode("000003").orElseThrow().id();
        assertThat(dsms.lister(anne, laubade)).allSatisfy(d -> {
            assertThat(d.dsm().facturee()).isFalse();
            assertThat(d.dsm().facturationDemandee()).isFalse();
        });
    }
}
