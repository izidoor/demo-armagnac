package fr.bnia.declarations.adapter.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TransitionDsm", description = "Envoi, prise en instruction ou validation d'une DSM (RG-DEC-20)")
public record TransitionDsmRequest(
        @Schema(description = "Version lue (verrou optimiste)") Long version) {
}
