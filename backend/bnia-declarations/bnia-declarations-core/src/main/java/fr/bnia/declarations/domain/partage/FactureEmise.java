package fr.bnia.declarations.domain.partage;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * {@code facturation} a émis la facture de la cotisation d'une DSM (A-DEC-4, ADR-013). Type reçu par
 * {@code declarations}, qui en tient une projection locale (RG-DEC-34) ; publié par {@code facturation}, qui
 * n'existe pas encore.
 *
 * @param dsm identifiant de la DSM facturée
 */
public record FactureEmise(long dsm, String numero, LocalDate date, BigDecimal ht, BigDecimal tva, BigDecimal ttc) {
}
