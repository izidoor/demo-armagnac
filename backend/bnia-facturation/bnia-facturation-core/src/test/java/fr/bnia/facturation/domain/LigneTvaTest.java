package fr.bnia.facturation.domain;

import static fr.bnia.facturation.domain.Exemples.EXONEREE;
import static fr.bnia.facturation.domain.Exemples.TVA_20;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import fr.bnia.facturation.domain.LigneTva.Montant;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LigneTvaTest {

    @Test
    @DisplayName("RG-FAC-05 : le récapitulatif d'une pièce regroupe par catégorie et taux, TVA sur la base")
    void rg_fac_05_recapituler() {
        List<LigneTva> recapitulatif = LigneTva.recapituler(List.of(new Montant(TVA_20, new BigDecimal("0.03")),
                new Montant(EXONEREE, new BigDecimal("10.00")), new Montant(TVA_20, new BigDecimal("0.04"))));

        assertThat(recapitulatif).extracting(LigneTva::categorie, LigneTva::base, LigneTva::tva).containsExactly(
                tuple(TVA_20, new BigDecimal("0.07"), new BigDecimal("0.01")),
                tuple(EXONEREE, new BigDecimal("10.00"), new BigDecimal("0.00")));
    }

    @Test
    @DisplayName("RG-FAC-30 : le récapitulatif de plusieurs pièces somme leurs TVA arrondies, pas la TVA de la base cumulée")
    void rg_fac_30_cumuler() {
        // Deux pièces de 0,03 € HT à 20 % : 0,01 € de TVA chacune (0,006 arrondi), 0,02 € en tout ; la base cumulée
        // de 0,06 € donnerait 0,01 €.
        List<LigneTva> piece = LigneTva.recapituler(List.of(new Montant(TVA_20, new BigDecimal("0.03"))));
        List<LigneTva> avoir = LigneTva.recapituler(List.of(new Montant(TVA_20, new BigDecimal("-0.03")),
                new Montant(EXONEREE, new BigDecimal("-5.00"))));

        assertThat(LigneTva.cumuler(List.of(piece, piece))).extracting(LigneTva::base, LigneTva::tva)
                .containsExactly(tuple(new BigDecimal("0.06"), new BigDecimal("0.02")));
        assertThat(LigneTva.cumuler(List.of(piece, piece, avoir)))
                .extracting(LigneTva::categorie, LigneTva::base, LigneTva::tva).containsExactly(
                        tuple(TVA_20, new BigDecimal("0.03"), new BigDecimal("0.01")),
                        tuple(EXONEREE, new BigDecimal("-5.00"), new BigDecimal("0.00")));
    }
}
