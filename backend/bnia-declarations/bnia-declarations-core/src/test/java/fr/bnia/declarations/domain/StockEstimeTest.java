package fr.bnia.declarations.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Stock estimé (spec 06 RG-CA-11) : la base vieillie à aujourd'hui. */
class StockEstimeTest {

    private static final LocalDate FIN_MARS = LocalDate.of(2026, 3, 31);

    private static LigneStockInitial ligne(CleConditionnement cle, Integer millesime, String denomination,
            String compte, String litres) {
        return new LigneStockInitial(IdentiteLigne.saisie(cle, millesime, denomination, compte, FIN_MARS),
                VolumeAlcoolPur.litres(litres));
    }

    private static String libelle(StockEstime.Ligne l) {
        IdentiteLigne id = l.identite();
        return id.cle() + " " + (id.millesime() != null ? id.millesime().annee() : id.denomination().texte()) + " "
                + id.compte() + " = " + l.estime();
    }

    private static final List<LigneStockInitial> BASE = List.of(
            ligne(CleConditionnement.MB, 2015, null, null, "816.80"),
            ligne(CleConditionnement.NB, null, "Hors d'âge", "19", "300"),
            ligne(CleConditionnement.NB, null, "Hors d'âge", "20+", "450"),
            ligne(CleConditionnement.MV, 2012, null, "10", "120"),
            ligne(CleConditionnement.NB, null, "Blanche", "00", "0"));

    @Test
    @DisplayName("RG-CA-11 : une base de fin mars vieillit au 1er avril suivant (MB recalculé, NB +1 borné à 20+, verre inchangé)")
    void rg_ca_11_base_vieillie_au_premier_avril() {
        StockEstime stock = StockEstime.depuis(BASE, FIN_MARS, FIN_MARS.plusDays(1), LocalDate.of(2026, 10, 23), List.of());

        assertThat(stock.baseAu()).contains(FIN_MARS);
        assertThat(stock.lignes()).extracting(StockEstimeTest::libelle).containsExactly(
                "MB 2015 10 = 816.80", "NB Hors d'âge 20+ = 750.00", "MV 2012 10 = 120.00");
        assertThat(stock.lignes()).allSatisfy(l -> {
            assertThat(l.achats().estNul()).isTrue();
            assertThat(l.ventes().estNul()).isTrue();
            assertThat(l.estime()).isEqualTo(l.base());
        });
    }

    @Test
    @DisplayName("RG-CA-11 : avant le 1er avril, la base ne vieillit pas ; sans base, le stock est nul")
    void rg_ca_11_sans_premier_avril_et_sans_base() {
        StockEstime stock = StockEstime.depuis(BASE, LocalDate.of(2025, 9, 30), LocalDate.of(2025, 10, 1),
                LocalDate.of(2026, 3, 31), List.of());
        assertThat(stock.lignes()).extracting(StockEstimeTest::libelle).containsExactly(
                "MB 2015 9 = 816.80", "NB Hors d'âge 19 = 300.00", "NB Hors d'âge 20+ = 450.00", "MV 2012 10 = 120.00");

        StockEstime sansBase = StockEstime.sansBase(LocalDate.of(2026, 3, 31), List.of());
        assertThat(sansBase.baseAu()).isEmpty();
        assertThat(sansBase.lignes()).isEmpty();
    }

    private static MouvementContrat mouvement(SensMouvementContrat sens, LocalDate date, CleConditionnement cle,
            Object grain, String compte, String litres) {
        IdentiteLigne identite = cle == CleConditionnement.MB
                ? IdentiteLigne.saisie(cle, (Integer) grain, null, null, date)
                : IdentiteLigne.saisie(cle, null, (String) grain, compte, date);
        return new MouvementContrat(1L, 7, sens, date, identite, VolumeAlcoolPur.litres(litres),
                sens == SensMouvementContrat.ACHAT ? ColonneDsm.C03 : ColonneDsm.C20, 3, "CA-2026-0001", 1, 8,
                java.util.Optional.empty());
    }

    @Test
    @DisplayName("RG-CA-11 : les mouvements de contrat datés après la base, même dans le futur, s'ajoutent (achats) ou se déduisent (ventes) ; ceux d'avant non")
    void rg_ca_11_mouvements_de_contrat() {
        LocalDate aujourdhui = LocalDate.of(2026, 10, 23);
        StockEstime stock = StockEstime.depuis(BASE, FIN_MARS, FIN_MARS.plusDays(1), aujourdhui, List.of(
                mouvement(SensMouvementContrat.VENTE, LocalDate.of(2026, 10, 20), CleConditionnement.MB, 2015, null, "705.00"),
                mouvement(SensMouvementContrat.VENTE, LocalDate.of(2026, 3, 15), CleConditionnement.MB, 2015, null, "100.00"),
                mouvement(SensMouvementContrat.ACHAT, LocalDate.of(2026, 10, 28), CleConditionnement.NB, "Hors d'age", "20+", "135.00"),
                mouvement(SensMouvementContrat.ACHAT, LocalDate.of(2026, 10, 21), CleConditionnement.NB, "XO", "10", "90.00")));

        assertThat(stock.lignes()).extracting(StockEstimeTest::libelle).containsExactly(
                "MB 2015 10 = 111.80", "NB Hors d'âge 20+ = 885.00", "MV 2012 10 = 120.00", "NB XO 10 = 90.00");
        StockEstime.Ligne mb = stock.lignes().getFirst();
        assertThat(mb.base()).isEqualTo(VolumeAlcoolPur.litres("816.80"));
        assertThat(mb.ventes()).isEqualTo(VolumeAlcoolPur.litres("705.00"));
        assertThat(StockEstime.sansBase(aujourdhui, List.of(mouvement(SensMouvementContrat.ACHAT,
                LocalDate.of(2026, 10, 21), CleConditionnement.NB, "XO", "10", "90.00"))).lignes())
                .extracting(StockEstimeTest::libelle).containsExactly("NB XO 10 = 90.00");
    }

    @Test
    @DisplayName("RG-CA-11 : un mouvement d'assemblage d'avant un 1er avril franchi est vieilli comme la base (NB +1)")
    void rg_ca_11_mouvement_vieilli() {
        StockEstime stock = StockEstime.depuis(List.of(), LocalDate.of(2026, 2, 28), LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 4, 2), List.of(mouvement(SensMouvementContrat.ACHAT, LocalDate.of(2026, 3, 10),
                        CleConditionnement.NB, "XO", "10", "90.00")));
        assertThat(stock.lignes()).extracting(StockEstimeTest::libelle).containsExactly("NB XO 11 = 90.00");
    }
}
