package fr.bnia.declarations.domain;

import static fr.bnia.declarations.domain.ColonneDsm.C17;
import static fr.bnia.declarations.domain.Exemples.dsm;
import static fr.bnia.declarations.domain.Exemples.dsmDeJuin;
import static fr.bnia.declarations.domain.Exemples.l;
import static fr.bnia.declarations.domain.Exemples.saisie;
import static fr.bnia.declarations.domain.Exemples.stock;
import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.declarations.domain.partage.CleConditionnement;
import fr.bnia.declarations.domain.partage.CompteDAgeDsm;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VieillissementTest {

    private static final LocalDate AVRIL_2025 = LocalDate.of(2025, 4, 1);
    private static final LocalDate AVRIL_2026 = LocalDate.of(2026, 4, 1);

    private static String ligne(LigneStockInitial l) {
        IdentiteLigne id = l.identite();
        return id.cle() + " " + (id.millesime() != null ? id.millesime().annee() : id.denomination().texte()) + " "
                + id.compte() + " = " + l.volume();
    }

    @Test
    @DisplayName("RG-DEC-14 : au 1er avril, compte MB recalculé depuis son millésime, NB +1 borné à 20+, MV et NV inchangés ; fusion à 20+")
    void rg_dec_14_vieillissement_et_fusion() {
        DeclarationStocksMouvements annee = dsm(TypeOperateur.PRODUCTEUR,
                PeriodeDeclaration.commencant(TypeDeclarant.PRODUCTEUR, AVRIL_2025), List.of(
                        stock(CleConditionnement.NB, "Hors d'âge", "19", "300"),
                        stock(CleConditionnement.NB, "hors d'age", "20+", "200.50"),
                        stock(CleConditionnement.MB, 2015, null, "100"),
                        stock(CleConditionnement.MV, 2015, "6", "50"),
                        stock(CleConditionnement.NV, "VSOP", "5", "40"),
                        stock(CleConditionnement.NB, "Blanche", "00", "25")));
        assertThat(annee.lignes().get(2).identite().compte()).isEqualTo(CompteDAgeDsm.de("9"));

        List<LigneStockInitial> reprise = Vieillissement.reprise(annee, AVRIL_2026);

        assertThat(reprise).extracting(VieillissementTest::ligne).containsExactly(
                "NB Hors d'âge 20+ = 500.50",
                "MB 2015 10 = 100.00",
                "MV 2015 6 = 50.00",
                "NV VSOP 5 = 40.00",
                "NB Blanche 0 = 25.00");
    }

    @Test
    @DisplayName("RG-DEC-14 : seules les lignes au stock de fin strictement positif sont reprises, sans vieillir hors du 1er avril")
    void rg_dec_14_reprise_du_stock_de_fin() {
        DeclarationStocksMouvements juin = dsmDeJuin();
        // XO vendu en entier, VSOP en partie.
        juin.enregistrer(saisie(juin, Map.of(0, Map.of(C17, l("1500")), 1, Map.of(C17, l("200.50")))), null);

        List<LigneStockInitial> reprise = Vieillissement.reprise(juin, LocalDate.of(2026, 7, 1));

        assertThat(reprise).hasSize(5).extracting(VieillissementTest::ligne).first().isEqualTo("NB VSOP 5 = 2000.00");
        assertThat(reprise).extracting(VieillissementTest::ligne).contains("NB Blanche 00 = 120.00");
    }

    @Test
    @DisplayName("RG-DEC-09 : après une interruption, NB prend +1 par 1er avril franchi, compte MB recalculé depuis son millésime")
    void rg_dec_09_vieillissement_apres_interruption() {
        DeclarationStocksMouvements annee = dsm(TypeOperateur.PRODUCTEUR,
                PeriodeDeclaration.commencant(TypeDeclarant.PRODUCTEUR, AVRIL_2025), List.of(
                        stock(CleConditionnement.NB, "Hors d'âge", "12", "300"),
                        stock(CleConditionnement.NB, "XO", "19", "50"),
                        stock(CleConditionnement.MB, 2015, null, "100"),
                        stock(CleConditionnement.NV, "VSOP", "5", "40")));

        // Fin le 31 mars 2026, reprise le 1er avril 2028 : trois 1er avril franchis (2026, 2027, 2028).
        List<LigneStockInitial> reprise = Vieillissement.apresInterruption(annee, LocalDate.of(2028, 4, 1));

        assertThat(reprise).extracting(VieillissementTest::ligne).containsExactly(
                "NB Hors d'âge 15 = 300.00",
                "NB XO 20+ = 50.00",
                "MB 2015 12 = 100.00",
                "NV VSOP 5 = 40.00");
        assertThat(Vieillissement.apresInterruption(annee, AVRIL_2026)).extracting(VieillissementTest::ligne)
                .as("sans interruption : la reprise ordinaire").containsExactlyElementsOf(
                        Vieillissement.reprise(annee, AVRIL_2026).stream().map(VieillissementTest::ligne).toList());
    }

    @Test
    @DisplayName("RG-DEC-09 : un négociant qui reprend en cours d'année vieillit des 1er avril franchis")
    void rg_dec_09_anniversaires() {
        assertThat(Vieillissement.anniversaires(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 1))).isZero();
        assertThat(Vieillissement.anniversaires(LocalDate.of(2026, 7, 1), LocalDate.of(2027, 4, 1))).isEqualTo(1);
        assertThat(Vieillissement.anniversaires(LocalDate.of(2026, 4, 1), LocalDate.of(2028, 3, 1))).isEqualTo(2);
    }
}
