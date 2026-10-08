package fr.bnia.referentiels.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

/**
 * Taux d'une classe de TVA sur une période (C.10.2.1 p.127) : un pourcentage entre 0 et 100, à deux
 * décimales au plus, valable du début à la fin incluses. Sans date de fin, il vaut pour toutes les
 * dates postérieures à son début (RG-REF-11).
 */
public record TauxTva(BigDecimal taux, LocalDate debut, LocalDate fin) {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public TauxTva {
        if (taux == null || taux.signum() < 0 || taux.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new RegleEnfreinte(null, "Le taux de TVA est un pourcentage entre 0 et 100.");
        }
        if (taux.stripTrailingZeros().scale() > 2) {
            throw new RegleEnfreinte(null, "Le taux de TVA a deux décimales au plus : " + taux + ".");
        }
        if (debut == null) {
            throw new RegleEnfreinte(null, "La date de début du taux est obligatoire.");
        }
        if (fin != null && fin.isBefore(debut)) {
            throw new RegleEnfreinte(null, "La date de fin du taux précède sa date de début.");
        }
        taux = taux.setScale(2, RoundingMode.UNNECESSARY);
    }

    public Optional<LocalDate> finEventuelle() {
        return Optional.ofNullable(fin);
    }

    /** Vrai si la date est dans la période, bornes incluses. */
    public boolean couvre(LocalDate date) {
        return !date.isBefore(debut) && (fin == null || !date.isAfter(fin));
    }

    /** RG-REF-11 : deux périodes ont au moins une date commune. */
    public boolean chevauche(TauxTva autre) {
        return (autre.fin == null || !debut.isAfter(autre.fin)) && (fin == null || !autre.debut.isAfter(fin));
    }

    /** « 20,00 % du 01/01/2014 au 31/12/2019 », ou « … à partir du 01/01/2014 » sans date de fin. */
    public String description() {
        String pourcentage = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.FRANCE)).format(taux);
        return pourcentage + " % " + (fin == null
                ? "à partir du " + debut.format(DATE)
                : "du " + debut.format(DATE) + " au " + fin.format(DATE));
    }
}
