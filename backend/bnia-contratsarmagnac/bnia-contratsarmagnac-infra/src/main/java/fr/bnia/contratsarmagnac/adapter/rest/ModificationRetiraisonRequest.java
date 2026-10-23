package fr.bnia.contratsarmagnac.adapter.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ModificationRetiraison", description = "Une nouvelle version de la retiraison, par la partie qui doit la valider (RG-CA-14)")
public record ModificationRetiraisonRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Numéro de la version vue, refusée si elle a changé")
        int versionVue,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) SaisieRetiraisonRequest contenu) {
}
