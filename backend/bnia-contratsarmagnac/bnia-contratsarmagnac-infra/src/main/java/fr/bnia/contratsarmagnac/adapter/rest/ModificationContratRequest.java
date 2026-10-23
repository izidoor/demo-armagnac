package fr.bnia.contratsarmagnac.adapter.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ModificationContrat", description = "Une nouvelle version du projet (RG-CA-07)")
public record ModificationContratRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Numéro de la version vue, refusée si elle a changé")
        int versionVue,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) SaisieContenuRequest contenu) {
}
