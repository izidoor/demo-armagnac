package fr.bnia.declarations.domain.partage;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Forme de comparaison d'une dénomination (RG-DEC-11) : sans casse, sans accents, espaces réduits ; {@code « Hors
 * d'âge »} donne {@code HORS D'AGE}. Publiée pour que les autres contextes (contrats, RG-CA-05) comparent leurs
 * dénominations à celles des DSM de la même façon.
 */
public final class DenominationNormalisee {

    private DenominationNormalisee() {
    }

    public static String de(String texte) {
        String sansAccents = Normalizer.normalize(texte, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sansAccents.toUpperCase(Locale.ROOT).strip().replaceAll("\\s+", " ");
    }
}
