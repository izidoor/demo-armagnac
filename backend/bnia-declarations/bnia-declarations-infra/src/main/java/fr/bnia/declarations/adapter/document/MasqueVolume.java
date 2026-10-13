package fr.bnia.declarations.adapter.document;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Le masque hl.l,cl des éditions (H-DEC-4, A-DEC-8) : 2 254,15 L s'écrivent {@code 22.54,15}, soit 22 hl,
 * 54 L et 15 cL. Présentation seulement : le domaine compte en litres.
 */
final class MasqueVolume {

    private MasqueVolume() {
    }

    static String format(BigDecimal litres) {
        BigDecimal centilitres = litres.setScale(2, RoundingMode.HALF_UP).movePointRight(2).abs();
        long cl = centilitres.longValueExact();
        String signe = litres.signum() < 0 ? "-" : "";
        return "%s%d.%02d,%02d".formatted(signe, cl / 10_000, cl / 100 % 100, cl % 100);
    }
}
