package fr.bnia.contratsarmagnac.adapter.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DecisionContrat", description = "Approbation ou refus de la version vue (RG-CA-08, 10)")
public record DecisionContratRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Numéro de la version vue, refusée si elle a changé")
        int versionVue) {
}
