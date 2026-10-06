package fr.bnia.referentiels.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Volume d'une contenance, en litres (§4.7.5 p.40) : strictement positif, trois décimales au plus
 * (RG-REF-10). Toujours en {@link BigDecimal}, ramené à trois décimales : {@code 0.7} et
 * {@code 0.700} sont le même volume.
 */
public record Volume(BigDecimal litres) {

    private static final int DECIMALES = 3;

    public Volume {
        if (litres == null || litres.signum() <= 0) {
            throw new RegleEnfreinte("RG-REF-10", "Le volume d'une contenance est strictement positif.");
        }
        if (litres.stripTrailingZeros().scale() > DECIMALES) {
            throw new RegleEnfreinte("RG-REF-10", "Le volume d'une contenance a trois décimales au plus : " + litres + ".");
        }
        litres = litres.setScale(DECIMALES, RoundingMode.UNNECESSARY);
    }

    public static Volume de(BigDecimal litres) {
        return new Volume(litres);
    }

    public static Volume de(String litres) {
        try {
            return new Volume(new BigDecimal(litres.strip().replace(',', '.')));
        } catch (NumberFormatException | NullPointerException e) {
            throw new RegleEnfreinte("RG-REF-10", "Volume invalide : « " + litres + " ».");
        }
    }

    /** Forme affichée : deux décimales au moins, virgule décimale, unité ({@code 0,70 L}, {@code 0,375 L}). */
    public String libelle() {
        DecimalFormat format = new DecimalFormat("0.00#", DecimalFormatSymbols.getInstance(Locale.FRANCE));
        return format.format(litres) + " L";
    }

    @Override
    public String toString() {
        return litres.toPlainString();
    }
}
