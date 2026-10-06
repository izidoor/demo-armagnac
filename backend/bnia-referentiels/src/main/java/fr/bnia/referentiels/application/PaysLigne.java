package fr.bnia.referentiels.application;

import fr.bnia.referentiels.domain.partage.LangueCertificat;
import fr.bnia.referentiels.domain.partage.NiveauDetailCertificat;

/**
 * Ligne du listing des pays (modèle de lecture, ADR-004).
 *
 * @param ageMinimum compte d'âge sous sa forme canonique ({@code 00}, {@code 0}, {@code 4}…), ou {@code null}
 */
public record PaysLigne(
        String code,
        String codeIso,
        String libelle,
        String zoneCommerciale,
        boolean membreUe,
        LangueCertificat langueCertificat,
        NiveauDetailCertificat niveauDetailCertificat,
        String ageMinimum,
        boolean actif) {
}
