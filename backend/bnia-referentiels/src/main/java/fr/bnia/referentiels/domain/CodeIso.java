package fr.bnia.referentiels.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Code pays ISO 3166-1 alpha-2 (H-REF-1) : deux lettres, mises en majuscules (RG-REF-06).
 */
public record CodeIso(String valeur) {

    private static final Pattern ALPHA_2 = Pattern.compile("[A-Z]{2}");

    public CodeIso {
        String normalise = valeur == null ? "" : valeur.strip().toUpperCase(Locale.ROOT);
        if (!ALPHA_2.matcher(normalise).matches()) {
            throw new RegleEnfreinte("RG-REF-06",
                    "Code ISO invalide : « " + (valeur == null ? "" : valeur.strip()) + " » (deux lettres, ISO 3166-1 alpha-2).");
        }
        valeur = normalise;
    }

    public static CodeIso de(String valeur) {
        return new CodeIso(valeur);
    }

    @Override
    public String toString() {
        return valeur;
    }
}
