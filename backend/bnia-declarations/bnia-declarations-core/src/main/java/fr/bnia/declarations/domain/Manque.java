package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import java.time.YearMonth;

/**
 * Une pièce manquante (RG-DEC-29), au niveau de la DSM : un justificatif pour un type de mouvement dont le
 * total est non nul, ou la DRM d'un mois de la période. Elle bloque la validation, pas l'envoi (H-DEC-13).
 *
 * @param typeMouvement type de mouvement sans justificatif, {@code null} pour une DRM
 * @param mois          mois sans DRM, {@code null} pour un justificatif
 * @param total         total du type de mouvement en cause, {@code null} pour une DRM
 */
public record Manque(TypeManque type, TypeMouvement typeMouvement, YearMonth mois, VolumeAlcoolPur total,
        String message) {

    public NiveauAnomalie niveau() {
        return NiveauAnomalie.DSM;
    }

    /** Ce qui manque. */
    public enum TypeManque {
        JUSTIFICATIF,
        DRM
    }
}
