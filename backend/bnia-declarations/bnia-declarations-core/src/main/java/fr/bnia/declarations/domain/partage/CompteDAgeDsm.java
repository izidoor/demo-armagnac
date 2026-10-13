package fr.bnia.declarations.domain.partage;

import fr.bnia.commun.domain.Millesime;
import fr.bnia.commun.domain.RegleEnfreinte;
import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Compte d'âge d'une ligne de DSM (glossaire, H-DEC-9, A-DEC-8) : {@code 00}, {@code 0}, {@code 1} …
 * {@code 19}, {@code 20+}. Un {@code CompteDAge} borné : tout compte de 20 ou plus vaut {@code 20+}
 * (§4.7 p.39-40). Ordonné {@code 00 < 0 < 1 < … < 20+}.
 */
public final class CompteDAgeDsm implements Comparable<CompteDAgeDsm> {

    public static final CompteDAgeDsm DOUBLE_ZERO = new CompteDAgeDsm(-1);
    public static final CompteDAgeDsm VINGT_ET_PLUS = new CompteDAgeDsm(20);

    private static final Pattern ENTIER = Pattern.compile("0|[1-9][0-9]{0,2}");

    /** {@code 00} vaut -1, {@code 20+} vaut 20, les autres leur nombre. */
    private final int rang;

    private CompteDAgeDsm(int rang) {
        this.rang = rang;
    }

    /**
     * RG-DEC-12 : {@code 00}, {@code 0} … {@code 19}, {@code 20+} ; un nombre de 20 ou plus vaut {@code 20+}.
     *
     * @throws RegleEnfreinte si la valeur n'est pas un compte d'âge
     */
    public static CompteDAgeDsm de(String valeur) {
        String texte = valeur == null ? "" : valeur.strip();
        if (texte.equals("00")) {
            return DOUBLE_ZERO;
        }
        if (texte.equals("20+")) {
            return VINGT_ET_PLUS;
        }
        if (!ENTIER.matcher(texte).matches()) {
            throw new RegleEnfreinte("RG-DEC-12",
                    "Compte d'âge invalide : « " + texte + " » (attendu : 00, 0, 1 … 19 ou 20+).");
        }
        return de(Integer.parseInt(texte));
    }

    /** {@code 0}, {@code 1}… ; 20 ou plus donne {@code 20+}. */
    public static CompteDAgeDsm de(int compte) {
        if (compte < 0) {
            throw new RegleEnfreinte("RG-DEC-12", "Compte d'âge invalide : " + compte + ".");
        }
        return new CompteDAgeDsm(Math.min(compte, 20));
    }

    /**
     * RG-DEC-12, annexe B (p.98-99) : compte d'un millésime {@code N} à une date. {@code 00} avant le
     * 1er avril {@code N+1}, puis « année d'observation − millésime − 1 », l'année d'observation étant
     * celle du dernier 1er avril ; borné à {@code 20+}.
     */
    public static CompteDAgeDsm calcule(Millesime millesime, LocalDate date) {
        int anneeObservation = date.getMonth().compareTo(Month.APRIL) >= 0 ? date.getYear() : date.getYear() - 1;
        int compte = anneeObservation - millesime.annee() - 1;
        return compte < 0 ? DOUBLE_ZERO : de(compte);
    }

    /** RG-DEC-14 : le compte un an plus tard ({@code 00} devient {@code 0}), borné à {@code 20+}. */
    public CompteDAgeDsm suivant() {
        return rang >= 20 ? VINGT_ET_PLUS : new CompteDAgeDsm(rang + 1);
    }

    public boolean estPlusVieuxQue(CompteDAgeDsm autre) {
        return compareTo(autre) > 0;
    }

    @Override
    public int compareTo(CompteDAgeDsm autre) {
        return Integer.compare(rang, autre.rang);
    }

    @Override
    public boolean equals(Object autre) {
        return autre instanceof CompteDAgeDsm compte && compte.rang == rang;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(rang);
    }

    /** Forme canonique : {@code 00}, {@code 0} … {@code 19}, {@code 20+}. */
    @Override
    public String toString() {
        return rang < 0 ? "00" : rang >= 20 ? "20+" : Integer.toString(rang);
    }
}
