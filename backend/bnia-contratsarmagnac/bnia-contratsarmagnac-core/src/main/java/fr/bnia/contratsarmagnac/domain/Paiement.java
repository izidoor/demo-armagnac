package fr.bnia.contratsarmagnac.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Paiement (CA-05 §4) : le solde, comptant ou à 30 jours fin de mois, et un acompte facultatif, en pourcentage.
 *
 * @param acompte dans ]0 ; 100[, à 2 décimales ; {@code null} sans acompte (RG-CA-03)
 */
public record Paiement(Solde solde, BigDecimal acompte) {

    public Paiement {
        if (solde == null) {
            throw new RegleEnfreinte("RG-CA-03", "Le paiement du solde est à choisir : comptant ou à 30 jours fin de mois.");
        }
        if (acompte != null) {
            if (acompte.signum() <= 0 || acompte.compareTo(BigDecimal.valueOf(100)) >= 0
                    || acompte.stripTrailingZeros().scale() > 2) {
                throw new RegleEnfreinte("RG-CA-03",
                        "L'acompte est un pourcentage strictement compris entre 0 et 100, à 2 décimales au plus.");
            }
            acompte = acompte.setScale(2, RoundingMode.UNNECESSARY);
        }
    }
}
