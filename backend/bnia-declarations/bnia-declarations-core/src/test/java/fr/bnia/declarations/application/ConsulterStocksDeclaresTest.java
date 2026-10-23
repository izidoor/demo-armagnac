package fr.bnia.declarations.application;

import static fr.bnia.declarations.application.Monde.AGENT_BNIA;
import static fr.bnia.declarations.application.Monde.AGENT_NEGOCIANT;
import static fr.bnia.declarations.application.Monde.NEGOCIANT;
import static fr.bnia.declarations.application.Monde.OBSERVATEUR_BNIA;
import static fr.bnia.declarations.application.Monde.OBSERVATEUR_NEGOCIANT;
import static fr.bnia.declarations.application.Monde.PRODUCTEUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.LigneDsm;
import fr.bnia.declarations.domain.SaisieDsm;
import fr.bnia.declarations.domain.SaisieLigneDsm;
import fr.bnia.declarations.domain.TypeDeclarant;
import fr.bnia.declarations.domain.partage.EtatDsm;
import fr.bnia.declarations.domain.partage.StockDeclareVue;
import fr.bnia.declarations.domain.partage.StockEstimeVue;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Stocks déclarés (spec 05 lot 8, UC-DEC-1) et lecture publiée de la fiche opérateur. */
class ConsulterStocksDeclaresTest {

    private final Monde monde = new Monde();
    private final GererDsm dsms = monde.gererDsm();
    private final ConsulterStocksDeclares stocks = monde.consulterStocks();

    @BeforeEach
    void pointDeDepart() {
        monde.gererPointDeDepart().fixer(AGENT_BNIA, NEGOCIANT, GererPointDeDepartTest.JUIN,
                GererPointDeDepartTest.STOCK);
    }

    private static SaisieLigneDsm telle(LigneDsm l, Map<ColonneDsm, VolumeAlcoolPur> volumes) {
        return new SaisieLigneDsm(l.id(), l.identite().cle(),
                l.identite().millesime() == null ? null : l.identite().millesime().annee(),
                l.identite().denomination() == null ? null : l.identite().denomination().texte(),
                l.identite().compte().toString(), volumes);
    }

    /** Juin : 500 L de XO vendus en France, tout le millésime 2010 aussi ; validée. */
    private long juinValidee() {
        DeclarationStocksMouvements juin = dsms.initier(AGENT_NEGOCIANT, NEGOCIANT).dsm();
        long id = juin.id();
        dsms.enregistrer(AGENT_NEGOCIANT, NEGOCIANT, id, new SaisieDsm(null, List.of(
                telle(juin.lignes().get(0), Map.of(ColonneDsm.C17, VolumeAlcoolPur.litres("500"))),
                telle(juin.lignes().get(1), Map.of(ColonneDsm.C17, VolumeAlcoolPur.litres("640.25"))))), null);
        dsms.envoyer(AGENT_NEGOCIANT, NEGOCIANT, id, null);
        dsms.prendreEnInstruction(AGENT_BNIA, NEGOCIANT, id, null);
        dsms.valider(AGENT_BNIA, NEGOCIANT, id, null);
        return id;
    }

    @Test
    @DisplayName("RG-DEC-42 : le stock de fin de la dernière DSM validée, ligne par ligne, sans les lignes finies à 0")
    void rg_dec_42_stock_declare() {
        assertThat(stocks.deLOperateur(AGENT_BNIA, NEGOCIANT)).as("sans DSM validée").isEmpty();
        long id = juinValidee();
        dsms.initier(AGENT_NEGOCIANT, NEGOCIANT);

        ConsulterStocksDeclares.StockDeclare stock = stocks.deLOperateur(OBSERVATEUR_NEGOCIANT, NEGOCIANT).orElseThrow();
        assertThat(stock.dsm()).isEqualTo(id);
        assertThat(stock.periode().fin()).isEqualTo(LocalDate.of(2026, 6, 30));
        assertThat(stock.valideeLe()).isNotNull();
        assertThat(stock.lignes()).singleElement().satisfies(l -> {
            assertThat(l.identite().denomination().texte()).isEqualTo("XO");
            assertThat(l.stock()).isEqualTo(VolumeAlcoolPur.litres("1000"));
        });

        assertThat(stocks.deTous(OBSERVATEUR_BNIA, Optional.empty(), Optional.empty())).singleElement()
                .satisfies(l -> {
                    assertThat(l.code()).isEqualTo("N7");
                    assertThat(l.raisonSociale()).isEqualTo("Opérateur 7");
                });
        assertThat(stocks.deTous(AGENT_BNIA, Optional.empty(), Optional.of(TypeDeclarant.PRODUCTEUR))).isEmpty();
        assertThat(stocks.deTous(AGENT_BNIA, Optional.of(PRODUCTEUR), Optional.empty())).isEmpty();
        assertThat(stocks.deLOperateur(AGENT_BNIA, PRODUCTEUR)).isEmpty();
    }

    @Test
    @DisplayName("RG-DEC-01 : tout le personnel du BNIA lit les stocks de tous ; un opérateur, les siens seulement")
    void rg_dec_01_refus() {
        assertThatThrownBy(() -> stocks.deTous(AGENT_NEGOCIANT, Optional.empty(), Optional.empty()))
                .isInstanceOf(AccesRefuse.class).hasMessageContaining("personnel du BNIA");
        assertThatThrownBy(() -> stocks.exporter(OBSERVATEUR_NEGOCIANT, Optional.empty(), Optional.empty()))
                .isInstanceOf(AccesRefuse.class);
        assertThatThrownBy(() -> stocks.deLOperateur(Monde.INCONNU, NEGOCIANT)).isInstanceOf(AccesRefuse.class);
        assertThatThrownBy(() -> stocks.deLOperateur(OBSERVATEUR_NEGOCIANT, PRODUCTEUR))
                .isInstanceOf(AccesRefuse.class);
    }

    @Test
    @DisplayName("UC-DEC-1 : l'export Excel reprend les lignes de l'écran, établi ce jour")
    void uc_dec_1_export() {
        juinValidee();
        assertThat(stocks.exporter(OBSERVATEUR_BNIA, Optional.empty(), Optional.empty())).isNotEmpty();
        assertThat(monde.editionsStocks.lignes).hasSize(1);
        assertThat(monde.editionsStocks.etabliLe).isEqualTo(LocalDate.of(2026, 10, 13));
    }

    @Test
    @DisplayName("RG-DEC-42 : lecture publiée de la dernière DSM et du stock déclaré, pour la fiche opérateur")
    void rg_dec_42_lecture_publiee() {
        DeclarationsPubliees publiees = monde.declarationsPubliees();
        assertThat(publiees.derniereDsm(NEGOCIANT)).isEmpty();
        assertThat(publiees.stockDeclare(NEGOCIANT)).isEmpty();
        long juin = juinValidee();
        long juillet = dsms.initier(AGENT_NEGOCIANT, NEGOCIANT).dsm().id();

        assertThat(publiees.derniereDsm(NEGOCIANT)).hasValueSatisfying(d -> {
            assertThat(d.dsm()).isEqualTo(juillet);
            assertThat(d.debut()).isEqualTo(LocalDate.of(2026, 7, 1));
            assertThat(d.etat()).isEqualTo(EtatDsm.BROUILLON);
        });
        StockDeclareVue stock = publiees.stockDeclare(NEGOCIANT).orElseThrow();
        assertThat(stock.dsm()).isEqualTo(juin);
        assertThat(stock.au()).isEqualTo(LocalDate.of(2026, 6, 30));
        assertThat(stock.lignes()).singleElement().satisfies(l -> {
            assertThat(l.denomination()).isEqualTo("XO");
            assertThat(l.compte().toString()).isEqualTo("12");
            assertThat(l.stock()).isEqualTo(VolumeAlcoolPur.litres("1000"));
        });
    }

    @Test
    @DisplayName("RG-CA-11 : stock estimé publié ; base du point de départ, puis de la dernière DSM validée")
    void rg_ca_11_stock_estime() {
        DeclarationsPubliees publiees = monde.declarationsPubliees();
        StockEstimeVue depart = publiees.stockEstime(NEGOCIANT);
        assertThat(depart.baseAu()).contains(GererPointDeDepartTest.JUIN);
        assertThat(depart.lignes()).extracting(l -> l.cle() + " " + l.estime())
                .containsExactly("NB 1500.00", "MB 640.25");

        juinValidee();
        StockEstimeVue apres = publiees.stockEstime(NEGOCIANT);
        assertThat(apres.baseAu()).contains(LocalDate.of(2026, 6, 30));
        assertThat(apres.lignes()).singleElement().satisfies(l -> {
            assertThat(l.denomination()).isEqualTo("XO");
            assertThat(l.base()).isEqualTo(VolumeAlcoolPur.litres("1000"));
            assertThat(l.estime()).isEqualTo(VolumeAlcoolPur.litres("1000"));
        });
        assertThat(stocks.estimeDe(OBSERVATEUR_NEGOCIANT, NEGOCIANT).lignes()).hasSize(1);
        assertThat(publiees.stockEstime(PRODUCTEUR).lignes()).as("ni DSM validée ni point de départ").isEmpty();
        assertThatThrownBy(() -> stocks.estimeDe(OBSERVATEUR_NEGOCIANT, PRODUCTEUR)).isInstanceOf(AccesRefuse.class);
    }
}
