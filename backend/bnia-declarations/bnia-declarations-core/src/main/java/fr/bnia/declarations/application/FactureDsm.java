package fr.bnia.declarations.application;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Un document de facturation d'une DSM, tenu en projection locale (A-DEC-4, RG-DEC-34, révisée par la spec 08
 * §3.13) : le brouillon demandé, puis la facture et les avoirs émis par {@code facturation}.
 *
 * @param piece  identifiant de la pièce chez {@code facturation}, pour l'ouvrir
 * @param numero {@code null} pour un brouillon, comme la date et les montants
 */
public record FactureDsm(long dsm, long piece, Type type, String numero, LocalDate date, BigDecimal ht,
        BigDecimal tva, BigDecimal ttc) {

    public enum Type {
        /** Facture demandée, pas encore émise. */
        BROUILLON,
        FACTURE,
        AVOIR
    }

    /** RG-DEC-43 : la facture demandée d'une DSM, en attente d'émission. */
    public static FactureDsm brouillon(long dsm, long piece) {
        return new FactureDsm(dsm, piece, Type.BROUILLON, null, null, null, null, null);
    }

    /** Le HT compté dans le total facturé de la DSM : négatif pour un avoir, nul pour un brouillon. */
    public BigDecimal htSigne() {
        return switch (type) {
            case BROUILLON -> BigDecimal.ZERO;
            case FACTURE -> ht;
            case AVOIR -> ht.negate();
        };
    }
}
