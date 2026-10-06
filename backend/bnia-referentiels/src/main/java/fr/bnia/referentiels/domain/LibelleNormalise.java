package fr.bnia.referentiels.domain;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Forme d'un libellé qui ignore la casse, les accents et les espaces superflus (RG-REF-15) :
 * « Château  x » et « CHATEAU X » ont la même forme normalisée, {@code CHATEAU X}.
 */
public record LibelleNormalise(String valeur) {

    public static LibelleNormalise de(Libelle libelle) {
        return de(libelle.valeur());
    }

    /** Normalise un texte quelconque, par exemple une saisie en cours. */
    public static LibelleNormalise de(String texte) {
        String sansAccents = Normalizer.normalize(texte, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return new LibelleNormalise(sansAccents.toUpperCase(Locale.ROOT).strip().replaceAll("\\s+", " "));
    }

    @Override
    public String toString() {
        return valeur;
    }
}
