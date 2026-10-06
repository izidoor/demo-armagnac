package fr.bnia.referentiels.adapter.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ModificationContenance", description = "Le volume est immuable (RG-REF-02) : seul le libellé change")
public record ModificationContenanceRequest(
        @Schema(description = "Facultatif : dérivé du volume si absent") String libelle) {
}
