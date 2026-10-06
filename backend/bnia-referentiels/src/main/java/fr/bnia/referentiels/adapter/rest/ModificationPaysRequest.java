package fr.bnia.referentiels.adapter.rest;

import fr.bnia.referentiels.application.SaisiePays;
import fr.bnia.referentiels.domain.partage.LangueCertificat;
import fr.bnia.referentiels.domain.partage.NiveauDetailCertificat;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ModificationPays", description = "Tout sauf le code, immuable (RG-REF-02)")
public record ModificationPaysRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "États-Unis") String libelle,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "US") String codeIso,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "AMN") String zoneCommerciale,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean membreUe,
        @Schema(description = "Absent : FR_EN (H-REF-2)") LangueCertificat langueCertificat,
        @Schema(description = "Absent : CUMUL_DEGRE (H-REF-2)") NiveauDetailCertificat niveauDetailCertificat,
        @Schema(description = "Compte d'âge : 00, 0, 1, 2… ; absent si aucun", example = "4") String ageMinimum) {

    SaisiePays versSaisie() {
        return new SaisiePays(libelle, codeIso, zoneCommerciale, membreUe, langueCertificat, niveauDetailCertificat,
                ageMinimum);
    }
}
