package fr.bnia.boot.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import fr.bnia.commun.domain.Appellation;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.contratsarmagnac.application.ConsulterContrats.Onglet;
import fr.bnia.contratsarmagnac.application.ConsulterContrats;
import fr.bnia.contratsarmagnac.application.NegocierContrats.SaisieContrat;
import fr.bnia.contratsarmagnac.application.NegocierContrats;
import fr.bnia.contratsarmagnac.application.SaisieContenu.SaisieLigne;
import fr.bnia.contratsarmagnac.application.SaisieContenu;
import fr.bnia.contratsarmagnac.domain.Partie;
import fr.bnia.contratsarmagnac.domain.Solde;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.support.AbstractIT;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ApplicationContext;

/**
 * Chargeur de démonstration des contrats (spec 06 §7.1), après les autres : relançable ; le projet d'Henri attend
 * Jeanne, qui l'approuve en un geste ; son stock estimé de 2015 bloque 30 hl à 47 % et accepte 15 hl ; l'historique
 * remplit les mercuriales.
 */
class DemoContratsIT extends AbstractIT {

    @Autowired ApplicationContext contexte;
    @Autowired ConsulterContrats consulter;
    @Autowired NegocierContrats negocier;

    @Test
    void ouvre_le_projet_d_henri_et_peut_etre_relance() throws Exception {
        var fabrique = contexte.getAutowireCapableBeanFactory();
        var chargeurs = List.of(fabrique.createBean(DemoPersonnelBnia.class), fabrique.createBean(DemoReferentiels.class),
                fabrique.createBean(DemoOperateurs.class), fabrique.createBean(DemoDeclarations.class),
                fabrique.createBean(DemoFacturation.class), fabrique.createBean(DemoMessagerie.class),
                fabrique.createBean(DemoContrats.class));
        for (int i = 0; i < 2; i++) {
            for (var chargeur : chargeurs) {
                ((ApplicationRunner) chargeur).run(new DefaultApplicationArguments());
            }
        }
        long durand = operateurs.operateurParCode("000001").orElseThrow().id();
        long gascogne = operateurs.operateurParCode("N320001").orElseThrow().id();
        Acteur jeanne = new Acteur(operateurs.utilisateurParEmail("admin@producteur.fr").orElseThrow());

        var aApprouver = consulter.duPortail(jeanne, durand).stream()
                .filter(c -> c.onglet().orElseThrow() == Onglet.A_APPROUVER).toList();
        assertThat(aApprouver).singleElement().satisfies(c -> {
            assertThat(c.ligne().produit()).isEqualTo("Hors d'âge 20+");
            assertThat(c.ligne().acheteur()).isEqualTo(gascogne);
        });

        // Le fil de la démonstration : 30 hl de 2015 à 47 % dépassent le stock estimé de Jeanne, 15 hl passent.
        assertThat(catchThrowable(() -> negocier.ouvrir(jeanne, durand,
                new SaisieContrat(Partie.VENDEUR, gascogne, millesime2015("30")))))
                .isInstanceOf(RegleEnfreinte.class).hasMessageContaining("8,17 hl AP en stock");
        assertThat(negocier.ouvrir(jeanne, durand, new SaisieContrat(Partie.VENDEUR, gascogne, millesime2015("15")))
                .partieQuiDoitApprouver()).contains(Partie.ACHETEUR);

        assertThat(negocier.approuver(jeanne, durand, aApprouver.getFirst().ligne().id(), 1).dateContrat())
                .isPresent();

        // Lot 3 : l'historique, vingt contrats exécutés sur les douze derniers mois, datés avant le mois courant, sans
        // mouvement de contrat ; ils remplissent les mercuriales.
        Acteur anne = new Acteur(DemoPersonnelBnia.anne(operateurs));
        var acheteurs = new java.util.HashSet<Long>();
        for (String code : List.of("N320002", "N400001")) {
            acheteurs.add(operateurs.operateurParCode(code).orElseThrow().id());
        }
        OperateursFacturesDemo.OPERATEURS.stream().filter(OperateursFacturesDemo.Operateur::negociant)
                .forEach(o -> acheteurs.add(operateurs.operateurParCode(o.code()).orElseThrow().id()));
        var executes = consulter.duBackOffice(anne, java.util.Optional.empty(), java.util.Optional.empty()).stream()
                .filter(c -> acheteurs.contains(c.ligne().acheteur()))
                .filter(c -> c.ligne().etat() == fr.bnia.contratsarmagnac.domain.EtatContrat.EXECUTE).toList();
        assertThat(executes).hasSize(DemoContrats.HISTORIQUE).allSatisfy(c -> {
            assertThat(c.ligne().dateContrat().orElseThrow())
                    .isBefore(java.time.LocalDate.now().withDayOfMonth(1))
                    .isAfter(java.time.LocalDate.now().minusMonths(12));
            assertThat(c.ligne().acheteur()).isNotIn(gascogne, durand);
        });
        assertThat(consulter.mercuriales(anne, java.util.Optional.empty(), java.util.Optional.empty()).produits())
                .hasSizeGreaterThan(3).allSatisfy(m -> assertThat(m.prixMoyen()).isBetween(m.prixMinimum(),
                        m.prixMaximum()));
    }

    private static SaisieContenu millesime2015(String volume) {
        return new SaisieContenu(List.of(new SaisieLigne(Appellation.BAS_ARMAGNAC, 2015, null, null,
                new BigDecimal(volume), new BigDecimal("47"), new BigDecimal("1750"))), null, null, null,
                Solde.COMPTANT, null);
    }
}
