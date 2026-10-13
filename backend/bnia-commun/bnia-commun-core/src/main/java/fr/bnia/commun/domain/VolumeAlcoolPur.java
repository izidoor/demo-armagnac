package fr.bnia.commun.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Volume d'alcool pur, en litres, au centilitre (glossaire « LAP » ; ADR-009 amendé, A-DEC-8 de la
 * spec 05) : arrondi au demi supérieur à 2 décimales. <b>Signé</b> : un stock calculé peut être négatif ;
 * la positivité d'un volume saisi est une règle de chaque contexte. Le masque hl.l,cl est de la
 * présentation (adapters d'édition et front).
 *
 * @param litres litres d'alcool pur, toujours à 2 décimales
 */
public record VolumeAlcoolPur(BigDecimal litres) implements Comparable<VolumeAlcoolPur> {

    public static final VolumeAlcoolPur ZERO = new VolumeAlcoolPur(BigDecimal.ZERO);

    private static final BigDecimal CENT = BigDecimal.valueOf(100);

    public VolumeAlcoolPur {
        Objects.requireNonNull(litres, "litres");
        litres = litres.setScale(2, RoundingMode.HALF_UP);
    }

    public static VolumeAlcoolPur litres(BigDecimal litres) {
        return new VolumeAlcoolPur(litres);
    }

    /** Pour les tests et les données écrites à la main : {@code litres("839.11")}. */
    public static VolumeAlcoolPur litres(String litres) {
        return new VolumeAlcoolPur(new BigDecimal(litres));
    }

    public VolumeAlcoolPur plus(VolumeAlcoolPur autre) {
        return new VolumeAlcoolPur(litres.add(autre.litres));
    }

    public VolumeAlcoolPur moins(VolumeAlcoolPur autre) {
        return new VolumeAlcoolPur(litres.subtract(autre.litres));
    }

    /** Hectolitres d'alcool pur (litres ÷ 100), sans arrondi : 839,11 L donnent 8,3911 hlAP. */
    public BigDecimal enHectolitres() {
        return litres.divide(CENT);
    }

    public boolean estNegatif() {
        return litres.signum() < 0;
    }

    public boolean estNul() {
        return litres.signum() == 0;
    }

    @Override
    public int compareTo(VolumeAlcoolPur autre) {
        return litres.compareTo(autre.litres);
    }

    /** En litres, au point décimal : {@code 839.11}. */
    @Override
    public String toString() {
        return litres.toPlainString();
    }
}
