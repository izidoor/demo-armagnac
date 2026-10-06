package fr.bnia.referentiels.adapter.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ModificationRegion", description = "Le code est immuable (RG-REF-02)")
public record ModificationRegionRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Gers") String libelle,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean enRegionDelimitee) {
}
