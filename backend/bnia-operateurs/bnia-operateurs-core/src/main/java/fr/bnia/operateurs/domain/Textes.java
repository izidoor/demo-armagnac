package fr.bnia.operateurs.domain;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Utilitaires de saisie : un texte facultatif vide vaut « non renseigné », et une forme normalisée
 * ignore la casse, les accents et les espaces superflus (RG-OPE-08, RG-OPE-09).
 */
public final class Textes {

    private Textes() {
    }

    /** Le texte sans espaces en tête ni en fin, ou {@code null} s'il est vide. */
    public static String facultatif(String texte) {
        return texte == null || texte.isBlank() ? null : texte.strip();
    }

    /** « Maison  Durand » et « MAISON DURAND » ont la même forme : {@code MAISON DURAND}. */
    public static String normalise(String texte) {
        String sansAccents = Normalizer.normalize(texte, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sansAccents.toUpperCase(Locale.ROOT).strip().replaceAll("\\s+", " ");
    }

    /** Le texte sans aucun espace, en majuscules : forme saisie des identifiants administratifs. */
    static String compact(String texte) {
        return texte.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }
}
