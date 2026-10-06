package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.math.BigDecimal;

/**
 * Coordonnées GPS en degrés décimaux (C.1.1.1 p.101) : latitude de -90 à 90, longitude de -180 à 180,
 * renseignées ensemble (RG-OPE-20).
 */
public record CoordonneesGps(BigDecimal latitude, BigDecimal longitude) {

    private static final BigDecimal NONANTE = BigDecimal.valueOf(90);
    private static final BigDecimal CENT_QUATRE_VINGTS = BigDecimal.valueOf(180);

    public CoordonneesGps {
        if (latitude == null || longitude == null) {
            throw new RegleEnfreinte("RG-OPE-20", "Latitude et longitude se renseignent ensemble.");
        }
        if (latitude.abs().compareTo(NONANTE) > 0) {
            throw new RegleEnfreinte("RG-OPE-20", "Latitude hors bornes : " + latitude + " (de -90 à 90).");
        }
        if (longitude.abs().compareTo(CENT_QUATRE_VINGTS) > 0) {
            throw new RegleEnfreinte("RG-OPE-20", "Longitude hors bornes : " + longitude + " (de -180 à 180).");
        }
    }

    /** {@code null} si ni la latitude ni la longitude ne sont saisies. */
    public static CoordonneesGps facultatives(BigDecimal latitude, BigDecimal longitude) {
        return latitude == null && longitude == null ? null : new CoordonneesGps(latitude, longitude);
    }
}
