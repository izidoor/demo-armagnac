package fr.bnia.referentiels.application;

import fr.bnia.referentiels.domain.partage.LangueCertificat;
import fr.bnia.referentiels.domain.partage.NiveauDetailCertificat;

/**
 * Saisie d'un pays, telle qu'elle arrive de l'écran.
 *
 * @param zoneCommerciale        identifiant de la zone commerciale ; {@code null} si non choisie
 * @param langueCertificat       {@code null} : valeur par défaut (H-REF-2)
 * @param niveauDetailCertificat {@code null} : valeur par défaut (H-REF-2)
 * @param ageMinimum             compte d'âge ({@code 00}, {@code 0}, {@code 4}…), facultatif
 */
public record SaisiePays(
        String code,
        String libelle,
        String codeIso,
        Long zoneCommerciale,
        boolean membreUe,
        LangueCertificat langueCertificat,
        NiveauDetailCertificat niveauDetailCertificat,
        String ageMinimum) {
}
