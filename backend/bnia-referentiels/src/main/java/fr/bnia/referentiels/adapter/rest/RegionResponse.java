package fr.bnia.referentiels.adapter.rest;

import fr.bnia.referentiels.application.RegionLigne;
import fr.bnia.referentiels.domain.Region;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Region")
public record RegionResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "GERS") String code,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Gers") String libelle,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean enRegionDelimitee,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean actif) {

    static RegionResponse depuis(Region region) {
        return new RegionResponse(region.code().valeur(), region.libelle().valeur(), region.estEnRegionDelimitee(),
                region.estActif());
    }

    static RegionResponse depuis(RegionLigne ligne) {
        return new RegionResponse(ligne.code(), ligne.libelle(), ligne.enRegionDelimitee(), ligne.actif());
    }
}
