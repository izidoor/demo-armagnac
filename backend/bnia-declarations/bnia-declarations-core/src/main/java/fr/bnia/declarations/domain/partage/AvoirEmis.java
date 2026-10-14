package fr.bnia.declarations.domain.partage;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * {@code facturation} a émis un avoir sur la cotisation d'une DSM (A-DEC-4, ADR-013, H-DEC-17). Type reçu par
 * {@code declarations}, qui en tient une projection locale (RG-DEC-34).
 *
 * @param dsm identifiant de la DSM
 * @param ht  montant de l'avoir, positif : il se retranche du total facturé
 */
public record AvoirEmis(long dsm, String numero, LocalDate date, BigDecimal ht, BigDecimal tva, BigDecimal ttc) {
}
