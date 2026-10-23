package fr.bnia.declarations.application;

import static fr.bnia.declarations.application.Monde.AGENT_BNIA;
import static fr.bnia.declarations.application.Monde.AGENT_NEGOCIANT;
import static fr.bnia.declarations.application.Monde.NEGOCIANT;
import static fr.bnia.declarations.application.Monde.PRODUCTEUR;
import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.ActionJournalDeclarations;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.LigneDsm;
import fr.bnia.declarations.domain.OrigineLigne;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import fr.bnia.declarations.domain.partage.CompteDAgeDsm;
import fr.bnia.declarations.domain.partage.RetiraisonValidee;
import fr.bnia.declarations.domain.partage.RetiraisonValidee.LigneRetiree;
import fr.bnia.declarations.domain.partage.StockEstimeVue;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Retiraisons validées reçues de {@code contratsarmagnac} (spec 06 UC-CA-4, UC-CA-7, RG-CA-11, 21 à 23). */
class InscrireRetiraisonsTest {

    private final Monde monde = new Monde();
    private final GererDsm dsms = monde.gererDsm();
    private final InscrireRetiraisons inscrire = monde.inscrireRetiraisons();

    @BeforeEach
    void pointDeDepart() {
        monde.gererPointDeDepart().fixer(AGENT_BNIA, NEGOCIANT, GererPointDeDepartTest.JUIN,
                GererPointDeDepartTest.STOCK);
    }

    /** Le producteur vend au négociant 7,05 hl AP de MB 2010 et 1,35 hl AP de Hors d'âge 20+. */
    private static RetiraisonValidee retiraison(int numero, LocalDate date) {
        return new RetiraisonValidee(3, "CA-2026-0001", numero, date, PRODUCTEUR, NEGOCIANT, 20, List.of(
                new LigneRetiree(CleConditionnement.MB, 2010, null, null, VolumeAlcoolPur.litres("705.00")),
                new LigneRetiree(CleConditionnement.NB, null, "Hors d'âge", CompteDAgeDsm.VINGT_ET_PLUS,
                        VolumeAlcoolPur.litres("135.00"))));
    }

    @Test
    @DisplayName("RG-CA-21, 22 : la DSM en brouillon qui couvre la date reçoit l'achat aussitôt (colonne 2, vendeur producteur), journalisé")
    void rg_ca_22_inscription_aussitot() {
        long juin = dsms.initier(AGENT_NEGOCIANT, NEGOCIANT).dsm().id();

        inscrire.retiraisonValidee(retiraison(1, LocalDate.of(2026, 6, 20)));

        DeclarationStocksMouvements dsm = monde.dsms.parId(juin).orElseThrow();
        assertThat(dsm.lignes().get(1).volume(ColonneDsm.C02)).isEqualTo(VolumeAlcoolPur.litres("705.00"));
        LigneDsm ajoutee = dsm.lignes().getLast();
        assertThat(ajoutee.origine()).isEqualTo(OrigineLigne.AJOUT);
        assertThat(ajoutee.id()).isNotNull();
        assertThat(ajoutee.volume(ColonneDsm.C02)).isEqualTo(VolumeAlcoolPur.litres("135.00"));
        assertThat(monde.mouvements.inscritsDans(juin)).hasSize(2);
        assertThat(monde.journal.deLaDsm(juin).getFirst()).satisfies(e -> {
            assertThat(e.action()).isEqualTo(ActionJournalDeclarations.CONTRAT_INSCRIT);
            assertThat(e.detail()).isEqualTo("Contrat CA-2026-0001, retiraison 1");
        });
        assertThat(monde.mouvements.deLOperateur(PRODUCTEUR)).as("le producteur n'a pas de DSM : en attente")
                .hasSize(2).allMatch(m -> m.dsm().isEmpty() && m.colonne() == ColonneDsm.C21);
    }

    @Test
    @DisplayName("RG-CA-22, 23 : un mouvement d'une période sans DSM s'inscrit à son initiation ; il justifie les achats à la validation")
    void rg_ca_22_inscription_a_l_initiation() {
        long juin = dsms.initier(AGENT_NEGOCIANT, NEGOCIANT).dsm().id();
        inscrire.retiraisonValidee(retiraison(2, LocalDate.of(2026, 7, 5)));
        assertThat(monde.mouvements.inscritsDans(juin)).isEmpty();
        dsms.envoyer(AGENT_NEGOCIANT, NEGOCIANT, juin, null);
        dsms.prendreEnInstruction(AGENT_BNIA, NEGOCIANT, juin, null);
        dsms.valider(AGENT_BNIA, NEGOCIANT, juin, null);

        VueDsm juillet = dsms.initier(AGENT_NEGOCIANT, NEGOCIANT);

        assertThat(juillet.dsm().lignes()).extracting(l -> l.volume(ColonneDsm.C02))
                .containsExactly(VolumeAlcoolPur.ZERO, VolumeAlcoolPur.litres("705.00"), VolumeAlcoolPur.litres("135.00"));
        assertThat(monde.mouvements.inscritsDans(juillet.dsm().id())).hasSize(2);
        assertThat(juillet.manques()).as("l'achat est justifié par le contrat").isEmpty();
        assertThat(monde.journal.deLaDsm(juillet.dsm().id()).getFirst().detail())
                .isEqualTo("Contrat CA-2026-0001, retiraison 2");
    }

    @Test
    @DisplayName("RG-CA-11 : le stock estimé compte les mouvements postérieurs à la base, inscrits ou non")
    void rg_ca_11_stock_estime_complet() {
        inscrire.retiraisonValidee(retiraison(1, LocalDate.of(2026, 10, 20)));

        StockEstimeVue acheteur = monde.declarationsPubliees().stockEstime(NEGOCIANT);
        assertThat(acheteur.lignes()).extracting(l -> l.cle() + " " + (l.millesime() != null ? l.millesime()
                : l.denomination()) + " " + l.compte() + " : " + l.base() + " + " + l.achats() + " - " + l.ventes())
                .containsExactly("NB XO 12 : 1500.00 + 0.00 - 0.00", "MB 2010 15 : 640.25 + 705.00 - 0.00",
                        "NB Hors d'âge 20+ : 0.00 + 135.00 - 0.00");
        StockEstimeVue vendeur = monde.declarationsPubliees().stockEstime(PRODUCTEUR);
        assertThat(vendeur.baseAu()).isEmpty();
        assertThat(vendeur.lignes()).extracting(StockEstimeVue.LigneStockEstimeVue::estime)
                .containsExactly(VolumeAlcoolPur.litres("-705.00"), VolumeAlcoolPur.litres("-135.00"));
    }
}
