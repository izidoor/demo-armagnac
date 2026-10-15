package fr.bnia.declarations.application;

import static fr.bnia.declarations.application.Monde.AGENT_BNIA;
import static fr.bnia.declarations.application.Monde.AGENT_NEGOCIANT;
import static fr.bnia.declarations.application.Monde.NEGOCIANT;
import static fr.bnia.declarations.application.Monde.OBSERVATEUR_BNIA;
import static fr.bnia.declarations.application.Monde.OBSERVATEUR_NEGOCIANT;
import static fr.bnia.declarations.application.Monde.PRODUCTEUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.application.ConsulterStocksDeclares.HistoriqueStocks;
import fr.bnia.declarations.application.ConsulterStocksDeclares.StockALaDate;
import fr.bnia.declarations.domain.AnneeDeclarative;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.LigneDsm;
import fr.bnia.declarations.domain.SaisieDsm;
import fr.bnia.declarations.domain.SaisieLigneDsm;
import fr.bnia.declarations.domain.TypeDeclarant;
import fr.bnia.declarations.domain.partage.EtatDsm;
import fr.bnia.declarations.domain.partage.StockDeclareVue;
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
    @DisplayName("RG-DEC-44 : à chaque fin de mois écoulée de l'année, le stock de la dernière DSM validée finie à cette date, reporté")
    void rg_dec_44_historique() {
        juinValidee();
        DeclarationStocksMouvements juillet = dsms.initier(AGENT_NEGOCIANT, NEGOCIANT).dsm();
        dsms.enregistrer(AGENT_NEGOCIANT, NEGOCIANT, juillet.id(), new SaisieDsm(null, List.of(
                telle(juillet.lignes().getFirst(), Map.of(ColonneDsm.C17, VolumeAlcoolPur.litres("100"))))), null);
        dsms.envoyer(AGENT_NEGOCIANT, NEGOCIANT, juillet.id(), null);
        dsms.prendreEnInstruction(AGENT_BNIA, NEGOCIANT, juillet.id(), null);
        dsms.valider(AGENT_BNIA, NEGOCIANT, juillet.id(), null);
        dsms.initier(AGENT_NEGOCIANT, NEGOCIANT);

        HistoriqueStocks historique = stocks.historique(OBSERVATEUR_BNIA, Optional.empty(), Optional.empty(),
                Optional.empty());
        assertThat(historique.annee()).isEqualTo(new AnneeDeclarative(2026));
        assertThat(historique.anneesDisponibles()).containsExactly(new AnneeDeclarative(2026));
        // Avril et mai : pas encore de DSM validée ; juin : 1 000 L de XO (le 2010, fini à 0, omis) ; juillet :
        // 900 L ; août et septembre, sans DSM validée : juillet reporté ; octobre, en cours, n'y est pas.
        assertThat(historique.lignes()).extracting(StockALaDate::au, l -> l.ligne().ligne().stock())
                .containsExactly(
                        tuple(LocalDate.of(2026, 6, 30), VolumeAlcoolPur.litres("1000")),
                        tuple(LocalDate.of(2026, 7, 31), VolumeAlcoolPur.litres("900")),
                        tuple(LocalDate.of(2026, 8, 31), VolumeAlcoolPur.litres("900")),
                        tuple(LocalDate.of(2026, 9, 30), VolumeAlcoolPur.litres("900")));
        assertThat(historique.lignes().getLast().ligne().ligne().dsm()).isEqualTo(juillet.id());
        assertThat(historique.lignes().getFirst().ligne().code()).isEqualTo("N7");

        assertThat(stocks.historiqueDe(OBSERVATEUR_NEGOCIANT, NEGOCIANT, Optional.empty()).lignes()).hasSize(4);
        assertThat(stocks.historique(AGENT_BNIA, Optional.empty(), Optional.empty(),
                Optional.of(TypeDeclarant.PRODUCTEUR)).lignes()).isEmpty();
        HistoriqueStocks precedente = stocks.historiqueDe(AGENT_BNIA, NEGOCIANT,
                Optional.of(new AnneeDeclarative(2025)));
        assertThat(precedente.annee().libelle()).isEqualTo("2025-2026");
        assertThat(precedente.lignes()).as("rien d'une autre année").isEmpty();
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
        assertThatThrownBy(() -> stocks.historique(AGENT_NEGOCIANT, Optional.empty(), Optional.empty(),
                Optional.empty())).isInstanceOf(AccesRefuse.class).hasMessageContaining("personnel du BNIA");
        assertThatThrownBy(() -> stocks.historiqueDe(OBSERVATEUR_NEGOCIANT, PRODUCTEUR, Optional.empty()))
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
}
