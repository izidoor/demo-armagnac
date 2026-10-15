package fr.bnia.facturation.domain;

import java.math.BigDecimal;

/** Totaux d'une pièce (RG-FAC-05) : HT des lignes article, TVA du récapitulatif, TTC = HT + TVA. */
public record Totaux(BigDecimal ht, BigDecimal tva, BigDecimal ttc) {
}
