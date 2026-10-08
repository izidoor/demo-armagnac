package fr.bnia.referentiels.adapter.rest;

import fr.bnia.commun.domain.CompteDAge;
import fr.bnia.referentiels.application.PaysLigne;
import fr.bnia.referentiels.domain.DescriptionPays;
import fr.bnia.referentiels.domain.Pays;
import fr.bnia.referentiels.domain.partage.LangueCertificat;
import fr.bnia.referentiels.domain.partage.NiveauDetailCertificat;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Pays")
public record PaysResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "12") long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "US") String code,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "US") String codeIso,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "États-Unis") String libelle,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Identifiant de la zone commerciale", example = "2") long zoneCommerciale,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean membreUe,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LangueCertificat langueCertificat,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) NiveauDetailCertificat niveauDetailCertificat,
        @Schema(description = "Compte d'âge : 00, 0, 1, 2…", example = "4") String ageMinimum,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean actif) {

    static PaysResponse depuis(Pays pays) {
        DescriptionPays d = pays.description();
        return new PaysResponse(pays.id(), pays.code().valeur(), d.codeIso().valeur(), pays.libelle().valeur(),
                d.zoneCommerciale(), d.membreUe(), d.langueCertificat(), d.niveauDetailCertificat(),
                d.ageMinimum().map(CompteDAge::toString).orElse(null), pays.estActif());
    }

    static PaysResponse depuis(PaysLigne l) {
        return new PaysResponse(l.id(), l.code(), l.codeIso(), l.libelle(), l.zoneCommerciale(), l.membreUe(),
                l.langueCertificat(), l.niveauDetailCertificat(), l.ageMinimum(), l.actif());
    }
}
