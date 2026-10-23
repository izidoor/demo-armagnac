package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.declarations.domain.partage.DenominationNormalisee;

/**
 * Dénomination d'une ligne {@code NB} ou {@code NV} (glossaire, H-DEC-8) : texte libre, non vide. Deux
 * dénominations sont égales sans tenir compte de la casse, des accents ni des espaces (RG-DEC-11) ; le
 * texte saisi est gardé pour l'affichage.
 *
 * @param texte     tel que saisi, sans espaces en tête ni en fin
 * @param normalisee forme de comparaison : {@code « Hors d'âge »} donne {@code HORS D'AGE}
 */
public record Denomination(String texte, String normalisee) {

    public static final int LONGUEUR_MAXIMUM = 100;

    /** @throws RegleEnfreinte si le texte est vide ou trop long (RG-DEC-10) */
    public static Denomination de(String texte) {
        String saisi = texte == null ? "" : texte.strip().replaceAll("\\s+", " ");
        if (saisi.isEmpty()) {
            throw new RegleEnfreinte("RG-DEC-10", "Une ligne NB ou NV porte une dénomination.");
        }
        if (saisi.length() > LONGUEUR_MAXIMUM) {
            throw new RegleEnfreinte("RG-DEC-10", "La dénomination « " + saisi.substring(0, 20) + "… » dépasse "
                    + LONGUEUR_MAXIMUM + " caractères.");
        }
        return new Denomination(saisi, normalise(saisi));
    }

    static String normalise(String texte) {
        return DenominationNormalisee.de(texte);
    }

    /** Égalité de RG-DEC-11 : sur la forme normalisée seulement. */
    @Override
    public boolean equals(Object autre) {
        return autre instanceof Denomination d && d.normalisee.equals(normalisee);
    }

    @Override
    public int hashCode() {
        return normalisee.hashCode();
    }

    @Override
    public String toString() {
        return texte;
    }
}
