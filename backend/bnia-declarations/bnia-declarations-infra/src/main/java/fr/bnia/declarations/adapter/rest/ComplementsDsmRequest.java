package fr.bnia.declarations.adapter.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ComplementsDsm", description = "Demande de compléments sur une DSM en instruction (RG-DEC-20)")
public record ComplementsDsmRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Motif, déposé dans la conversation de la DSM, protégé (RG-MSG-08)",
                example = "DAE manquants pour l'export") String motif,
        @Schema(description = "Version lue (verrou optimiste)") Long version) {
}
