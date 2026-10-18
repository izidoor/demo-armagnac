package fr.bnia.facturation.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Arrondis des montants d'une pièce (RG-FAC-05). */
final class Montants {

    static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
    private static final BigDecimal CENT = BigDecimal.valueOf(100);

    private Montants() {
    }

    /** Au centime, demi supérieur. */
    static BigDecimal centimes(BigDecimal montant) {
        return montant.setScale(2, RoundingMode.HALF_UP);
    }

    /** Base × taux (pourcentage), au centime, demi supérieur. */
    static BigDecimal pourcentage(BigDecimal base, BigDecimal taux) {
        return centimes(base.multiply(taux).divide(CENT));
    }
}
