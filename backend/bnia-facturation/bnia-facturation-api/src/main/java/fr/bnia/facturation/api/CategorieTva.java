package fr.bnia.facturation.api;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Catégorie de TVA d'une ligne article (spec 08 §3.3), sous-ensemble de la liste UNCL5305 d'EN 16931.
 *
 * @param taux  pourcentage, 2 décimales : strictement positif pour {@code S}, nul pour {@code Z} et {@code E},
 *              absent ({@code null}) pour {@code O}
 * @param motif motif d'exonération (BT-120), pour {@code E} et {@code O} seulement ; imprimé au récapitulatif
 */
public record CategorieTva(Code code, BigDecimal taux, String motif) {

    /** Code de la catégorie (UNCL5305). */
    public enum Code {
        /** Taux normal ou réduit. */
        S,
        /** Taux zéro. */
        Z,
        /** Exonérée. */
        E,
        /** Hors champ. */
        O
    }

    public CategorieTva {
        if (code == null) {
            throw new RegleEnfreinte("RG-FAC-07", "La catégorie de TVA est obligatoire.");
        }
        motif = motif == null || motif.isBlank() ? null : motif.strip();
        switch (code) {
            case S -> {
                if (taux == null || taux.signum() <= 0) {
                    throw new RegleEnfreinte("RG-FAC-07", "Une TVA au taux normal ou réduit a un taux strictement positif.");
                }
            }
            case Z, E -> {
                if (taux == null || taux.signum() != 0) {
                    throw new RegleEnfreinte("RG-FAC-07", "Une TVA à taux zéro ou exonérée a un taux nul.");
                }
            }
            case O -> {
                if (taux != null) {
                    throw new RegleEnfreinte("RG-FAC-07", "Une opération hors champ de la TVA n'a pas de taux.");
                }
            }
        }
        boolean motive = code == Code.E || code == Code.O;
        if (motive && motif == null) {
            throw new RegleEnfreinte("RG-FAC-07", "Une opération exonérée ou hors champ a un motif d'exonération.");
        }
        if (!motive && motif != null) {
            throw new RegleEnfreinte("RG-FAC-07", "Seule une opération exonérée ou hors champ a un motif d'exonération.");
        }
        taux = taux == null ? null : taux.setScale(2, RoundingMode.HALF_UP);
    }

    public static CategorieTva normale(BigDecimal taux) {
        return new CategorieTva(Code.S, taux, null);
    }

    public static CategorieTva exoneree(String motif) {
        return new CategorieTva(Code.E, BigDecimal.ZERO, motif);
    }

    /** Le taux, nul pour {@code O} : de quoi calculer la TVA d'une base. */
    public BigDecimal tauxApplique() {
        return taux == null ? BigDecimal.ZERO.setScale(2) : taux;
    }

    /** « 20,00 % », « Exonérée », « Hors champ », « 0,00 % ». */
    public String libelle() {
        return switch (code) {
            case S, Z -> new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.FRANCE)).format(taux) + " %";
            case E -> "Exonérée";
            case O -> "Hors champ";
        };
    }
}
