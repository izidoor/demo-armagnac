package fr.bnia.contratsarmagnac.domain;

import static fr.bnia.contratsarmagnac.domain.Exemples.AUJOURDHUI;
import static fr.bnia.contratsarmagnac.domain.Exemples.assemblage;
import static fr.bnia.contratsarmagnac.domain.Exemples.contenu;
import static fr.bnia.contratsarmagnac.domain.Exemples.millesime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.Appellation;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LigneContratTest {

    @Test
    @DisplayName("RG-CA-04 : exemple du CA-05, 30 hl à 47 % et 1 750 € / hl AP donnent 1 410 L AP, 8,23 € / l vol, 24 675 € HT ; 2009 donne 16")
    void rg_ca_04_calculs_du_ca_05() {
        LigneContrat ligne = millesime(2009, "30", "47", "1750");
        assertThat(ligne.volumeAp()).isEqualTo(VolumeAlcoolPur.litres("1410"));
        assertThat(ligne.prixLitreVolume()).isEqualByComparingTo("8.23");
        assertThat(ligne.montantHt()).isEqualByComparingTo("24675.00");
        assertThat(ligne.compte().toString()).isEqualTo("16");

        LigneContrat seconde = millesime(2018, "30", "48.20", "1300");
        assertThat(seconde.volumeAp()).isEqualTo(VolumeAlcoolPur.litres("1446"));
        assertThat(seconde.compte().toString()).isEqualTo("7");
        ContenuContrat contenu = contenu(ligne, seconde);
        assertThat(contenu.montantHt()).isEqualByComparingTo("43473.00");
        assertThat(contenu.volumeAp()).isEqualTo(VolumeAlcoolPur.litres("2856"));
    }

    @Test
    @DisplayName("RG-CA-04 : le compte d'un millésime se calcule à la date de la version (annexe B)")
    void rg_ca_04_compte_a_la_date() {
        LigneContrat avantAvril = LigneContrat.saisie(Appellation.ARMAGNAC, 2009, null, null, BigDecimal.ONE,
                BigDecimal.TEN, BigDecimal.TEN, LocalDate.of(2026, 3, 31));
        assertThat(avantAvril.compte().toString()).isEqualTo("15");
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = ';', value = {
            "volume nul;0;47;1750", "volume à 3 décimales;1.001;47;1750", "degré nul;1;0;1750",
            "degré au-delà de 100;1;100.01;1750", "prix nul;1;47;0"})
    @DisplayName("RG-CA-03 : volume et prix strictement positifs, degré dans ]0 ; 100], 2 décimales")
    void rg_ca_03_nombres(String cas, String volume, String degre, String prix) {
        assertThatThrownBy(() -> millesime(2015, volume, degre, prix)).isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-CA-03");
    }

    @Test
    @DisplayName("RG-CA-03 : millésime ou dénomination, pas les deux ; compte saisi pour une dénomination ; au moins une ligne")
    void rg_ca_03_identite_et_lignes() {
        assertThatThrownBy(() -> LigneContrat.saisie(Appellation.ARMAGNAC, 2015, "XO", "10", BigDecimal.ONE,
                BigDecimal.TEN, BigDecimal.TEN, AUJOURDHUI)).hasMessageContaining("pas les deux");
        assertThatThrownBy(() -> LigneContrat.saisie(Appellation.ARMAGNAC, null, " ", null, BigDecimal.ONE,
                BigDecimal.TEN, BigDecimal.TEN, AUJOURDHUI)).hasMessageContaining("millésime ou une dénomination");
        assertThatThrownBy(() -> assemblage("XO", null, "1", "40", "1000")).hasMessageContaining("compte d'âge");
        assertThatThrownBy(() -> assemblage("XO", "abc", "1", "40", "1000")).isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-CA-03");
        assertThatThrownBy(() -> millesime(2027, "1", "40", "1000")).hasMessageContaining("futur");
        assertThatThrownBy(() -> LigneContrat.saisie(null, 2015, null, null, BigDecimal.ONE, BigDecimal.TEN,
                BigDecimal.TEN, AUJOURDHUI)).hasMessageContaining("appellation");
        assertThatThrownBy(() -> new ContenuContrat(List.of(), null, null, null,
                new Paiement(Solde.COMPTANT, null))).hasMessageContaining("au moins une ligne");
    }

    @Test
    @DisplayName("RG-CA-03 : acompte facultatif, dans ]0 ; 100[ % ; solde obligatoire")
    void rg_ca_03_paiement() {
        assertThat(new Paiement(Solde.TRENTE_JOURS_FIN_DE_MOIS, new BigDecimal("30")).acompte()).isEqualTo("30.00");
        assertThatThrownBy(() -> new Paiement(Solde.COMPTANT, new BigDecimal("100")))
                .hasMessageContaining("acompte");
        assertThatThrownBy(() -> new Paiement(Solde.COMPTANT, BigDecimal.ZERO)).hasMessageContaining("acompte");
        assertThatThrownBy(() -> new Paiement(null, null)).hasMessageContaining("solde");
    }

    @Test
    @DisplayName("RG-CA-05 : toujours sous bois ; MB et le millésime, ou NB, la dénomination normalisée et le compte ; l'appellation n'y entre pas")
    void rg_ca_05_grain() {
        assertThat(millesime(2015, "1", "40", "1000").grain()).isEqualTo(Grain.millesime(2015))
                .extracting(Grain::cle).isEqualTo(CleConditionnement.MB);
        Grain horsDAge = assemblage("Hors d'âge", "20+", "1", "40", "1000").grain();
        assertThat(horsDAge.cle()).isEqualTo(CleConditionnement.NB);
        assertThat(assemblage("  hors   d'AGE ", "25", "1", "40", "1000").grain()).isEqualTo(horsDAge);
        assertThat(assemblage("Hors d'âge", "19", "1", "40", "1000").grain()).isNotEqualTo(horsDAge);
        LigneContrat autreAppellation = LigneContrat.saisie(Appellation.HAUT_ARMAGNAC, 2015, null, null,
                BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN, AUJOURDHUI);
        assertThat(autreAppellation.grain()).isEqualTo(Grain.millesime(2015));
    }
}
