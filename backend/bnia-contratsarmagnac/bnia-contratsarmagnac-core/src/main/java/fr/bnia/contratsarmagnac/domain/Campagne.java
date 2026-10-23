package fr.bnia.contratsarmagnac.domain;

import java.time.LocalDate;
import java.time.Month;

/** Campagne d'un contrat (H-CA-6, CA-05) : du 1er avril au 31 mars ; le 13 octobre 2026 est dans 2026-2027. */
public final class Campagne {

    private Campagne() {
    }

    public static String de(LocalDate date) {
        int debut = date.getMonth().compareTo(Month.APRIL) >= 0 ? date.getYear() : date.getYear() - 1;
        return debut + "-" + (debut + 1);
    }
}
