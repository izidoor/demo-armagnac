package fr.bnia.facturation.adapter.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ParametrageFacturation", description = "Paramétrage de la facturation (spec 08 §6.1)")
public record ParametrageFacturationResponse(
        @Schema(description = "Vide tant qu'il n'est pas paramétré : l'émission est alors refusée (RG-FAC-09)")
        VendeurDto vendeur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "L'utilisateur connecté peut le modifier (RG-FAC-26)")
        boolean modifiable) {
}
