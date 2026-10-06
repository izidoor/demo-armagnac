package fr.bnia.referentiels.domain;

import fr.bnia.commun.domain.CompteDAge;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.referentiels.domain.partage.LangueCertificat;
import fr.bnia.referentiels.domain.partage.NiveauDetailCertificat;
import java.util.Objects;
import java.util.Optional;

/**
 * Attributs propres d'un pays, en plus du code, du libellé et de l'état actif de toute valeur.
 *
 * @param zoneCommerciale        code de la zone commerciale, son continent (H-REF-5)
 * @param membreUe               indépendant de la zone commerciale (RG-REF-08)
 * @param langueCertificat       {@code null} : {@link LangueCertificat#PAR_DEFAUT} (RG-REF-07, H-REF-2)
 * @param niveauDetailCertificat {@code null} : {@link NiveauDetailCertificat#PAR_DEFAUT} (RG-REF-07, H-REF-2)
 * @param ageMinimum             facultatif (RG-REF-09)
 */
public record DescriptionPays(
        CodeIso codeIso,
        Code zoneCommerciale,
        boolean membreUe,
        LangueCertificat langueCertificat,
        NiveauDetailCertificat niveauDetailCertificat,
        Optional<CompteDAge> ageMinimum) {

    public DescriptionPays {
        Objects.requireNonNull(codeIso);
        Objects.requireNonNull(zoneCommerciale);
        langueCertificat = langueCertificat == null ? LangueCertificat.PAR_DEFAUT : langueCertificat;
        niveauDetailCertificat = niveauDetailCertificat == null ? NiveauDetailCertificat.PAR_DEFAUT : niveauDetailCertificat;
        ageMinimum = ageMinimum == null ? Optional.empty() : ageMinimum;
    }

    /**
     * RG-REF-09 : l'âge minimum, s'il est saisi, est un compte d'âge valide ; la validation appartient
     * à {@link CompteDAge}.
     */
    public static Optional<CompteDAge> ageMinimum(String saisie) {
        if (saisie == null || saisie.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(CompteDAge.de(saisie));
        } catch (RegleEnfreinte e) {
            throw new RegleEnfreinte("RG-REF-09", "Âge minimum : " + e.getMessage());
        }
    }
}
