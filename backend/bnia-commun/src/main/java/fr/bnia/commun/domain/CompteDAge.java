package fr.bnia.commun.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Âge commercial d'un Armagnac (glossaire, p.39 et 98-99) : {@code 00}, {@code 0}, {@code 1},
 * {@code 2}…
 *
 * <p>Ce n'est pas un entier : {@code 00} et {@code 0} sont deux valeurs distinctes, ordonnées
 * {@code 00 < 0 < 1}. Sans borne supérieure ici ; la borne {@code 20+} de la DSM appartient à
 * {@code declarations}. Concept partagé par plusieurs contextes (ADR-009).
 */
public final class CompteDAge implements Comparable<CompteDAge> {

    public static final CompteDAge DOUBLE_ZERO = new CompteDAge(-1);
    public static final CompteDAge ZERO = new CompteDAge(0);

    private static final Pattern ENTIER = Pattern.compile("0|[1-9][0-9]{0,2}");

    /** {@code 00} vaut -1 ; les autres comptes valent leur nombre. */
    private final int rang;

    private CompteDAge(int rang) {
        this.rang = rang;
    }

    /**
     * @throws RegleEnfreinte si la valeur n'est pas un compte d'âge
     */
    public static CompteDAge de(String valeur) {
        String texte = valeur == null ? "" : valeur.strip();
        if (texte.equals("00")) {
            return DOUBLE_ZERO;
        }
        if (!ENTIER.matcher(texte).matches()) {
            throw new RegleEnfreinte(null,
                    "Compte d'âge invalide : « " + texte + " » (attendu : 00, 0, 1, 2…).");
        }
        return de(Integer.parseInt(texte));
    }

    /** Compte d'âge {@code 0}, {@code 1}, {@code 2}… ; {@code 00} ne s'obtient que par {@link #de(String)}. */
    public static CompteDAge de(int compte) {
        if (compte < 0) {
            throw new RegleEnfreinte(null, "Compte d'âge invalide : " + compte + ".");
        }
        return compte == 0 ? ZERO : new CompteDAge(compte);
    }

    public boolean estPlusJeuneQue(CompteDAge autre) {
        return compareTo(autre) < 0;
    }

    @Override
    public int compareTo(CompteDAge autre) {
        return Integer.compare(rang, autre.rang);
    }

    @Override
    public boolean equals(Object autre) {
        return autre instanceof CompteDAge compte && compte.rang == rang;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(rang);
    }

    /** Forme canonique : {@code 00}, {@code 0}, {@code 1}… */
    @Override
    public String toString() {
        return rang < 0 ? "00" : Integer.toString(rang);
    }
}
