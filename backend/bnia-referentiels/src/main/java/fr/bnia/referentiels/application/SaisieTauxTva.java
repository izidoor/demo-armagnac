package fr.bnia.referentiels.application;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Saisie d'un taux de TVA.
 *
 * @param fin facultative : sans fin, le taux vaut pour toutes les dates postérieures à son début
 */
public record SaisieTauxTva(BigDecimal taux, LocalDate debut, LocalDate fin) {
}
