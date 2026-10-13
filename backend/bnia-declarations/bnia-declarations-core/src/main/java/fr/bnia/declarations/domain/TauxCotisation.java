package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Un taux de la grille CVO (spec 05 §3.5, C.10.3.1 p.128) : montant en €/hlAP, pourcentage exonéré de TVA,
 * date de début. Sa fin n'est pas saisie : c'est la veille du début du taux suivant (RG-DEC-30).
 *
 * @param montant            €/hlAP, strictement positif, deux décimales au plus
 * @param pourcentageExonere part du montant non assujettie à la TVA, de 0 à 100, deux décimales au plus
 */
public record TauxCotisation(BigDecimal montant, BigDecimal pourcentageExonere, LocalDate debut) {

    private static final BigDecimal CENT = BigDecimal.valueOf(100);

    public TauxCotisation {
        if (montant == null || montant.signum() <= 0) {
            throw new RegleEnfreinte("RG-DEC-30", "Le montant du taux CVO est strictement positif, en €/hlAP.");
        }
        if (pourcentageExonere == null || pourcentageExonere.signum() < 0 || pourcentageExonere.compareTo(CENT) > 0) {
            throw new RegleEnfreinte("RG-DEC-30", "Le pourcentage exonéré de TVA va de 0 à 100.");
        }
        if (montant.stripTrailingZeros().scale() > 2 || pourcentageExonere.stripTrailingZeros().scale() > 2) {
            throw new RegleEnfreinte("RG-DEC-30", "Le montant et le pourcentage ont deux décimales au plus.");
        }
        if (debut == null) {
            throw new RegleEnfreinte("RG-DEC-30", "La date de début du taux CVO est obligatoire.");
        }
        montant = montant.setScale(2, RoundingMode.UNNECESSARY);
        pourcentageExonere = pourcentageExonere.setScale(2, RoundingMode.UNNECESSARY);
    }

    /** « 60,00 €/hlAP » : pour les messages. */
    public String description() {
        return new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.FRANCE)).format(montant) + " €/hlAP";
    }
}
