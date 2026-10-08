package fr.bnia.referentiels.adapter.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "SaisieContenance", description = "Création ou modification d'une contenance, volume compris")
public record SaisieContenanceRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "En litres, trois décimales au plus", example = "0.7") BigDecimal volume,
        @Schema(description = "Facultatif : dérivé du volume si absent") String libelle) {
}
