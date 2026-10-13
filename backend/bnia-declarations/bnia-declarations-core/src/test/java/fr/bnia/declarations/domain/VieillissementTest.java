package fr.bnia.declarations.domain;

import static fr.bnia.declarations.domain.ColonneDsm.C17;
import static fr.bnia.declarations.domain.Exemples.dsm;
import static fr.bnia.declarations.domain.Exemples.dsmDeJuin;
import static fr.bnia.declarations.domain.Exemples.l;
import static fr.bnia.declarations.domain.Exemples.saisie;
import static fr.bnia.declarations.domain.Exemples.stock;
import static org.assertj.core.api.Assertions.assertThat;

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
    @DisplayName("RG-DEC-14 : au 1er avril, MB recalculé, NB +1 borné à 20+, MV et NV inchangés ; fusion à 20+")
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
}
