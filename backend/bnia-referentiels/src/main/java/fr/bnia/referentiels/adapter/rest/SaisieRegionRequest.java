package fr.bnia.referentiels.adapter.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SaisieRegion", description = "Création ou modification d'une région, code compris")
public record SaisieRegionRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "GERS") String code,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Gers") String libelle,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean enRegionDelimitee) {
}
