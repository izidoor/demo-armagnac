package fr.bnia.contratsarmagnac.adapter.rest;

import fr.bnia.contratsarmagnac.domain.Partie;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SaisieContrat", description = "Un projet de contrat ouvert par l'opérateur actif (UC-CA-1)")
public record SaisieContratRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "La partie que tient l'opérateur actif")
        Partie partie,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "L'autre opérateur (recherche restreinte)")
        long autrePartie,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) SaisieContenuRequest contenu) {
}
