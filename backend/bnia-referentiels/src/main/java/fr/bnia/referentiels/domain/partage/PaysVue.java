package fr.bnia.referentiels.domain.partage;

import fr.bnia.commun.domain.CompteDAge;
import java.util.Optional;

/**
 * Pays vu par les autres contextes, notamment {@code exportation} (certificat, âge minimum) et
 * {@code statistiques} (zone, UE).
 *
 * @param zoneCommerciale code de la zone commerciale, le continent (H-REF-5)
 * @param ageMinimum      âge minimum à l'export ; une vente plus jeune déclenche une alerte non
 *                        bloquante, dans {@code exportation} (Q-REF-8)
 */
public record PaysVue(
        String code,
        String codeIso,
        String libelle,
        String zoneCommerciale,
        boolean membreUe,
        LangueCertificat langueCertificat,
        NiveauDetailCertificat niveauDetailCertificat,
        Optional<CompteDAge> ageMinimum,
        boolean actif) {
}
