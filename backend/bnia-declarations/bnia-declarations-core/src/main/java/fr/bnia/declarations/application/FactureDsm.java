package fr.bnia.declarations.application;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Une facture ou un avoir d'une DSM, émis par {@code facturation} et tenu en projection locale (A-DEC-4,
 * RG-DEC-34).
 */
public record FactureDsm(long dsm, Type type, String numero, LocalDate date, BigDecimal ht, BigDecimal tva,
        BigDecimal ttc) {

    public enum Type {
        FACTURE,
        AVOIR
    }

    /** Le HT compté dans le total facturé de la DSM : négatif pour un avoir. */
    public BigDecimal htSigne() {
        return type == Type.AVOIR ? ht.negate() : ht;
    }
}
