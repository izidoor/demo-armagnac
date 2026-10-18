package fr.bnia.declarations.adapter.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SaisieNoteInterneDsm", description = "Note interne écrite ou modifiée (RG-DEC-24)")
public record NoteInterneRequest(
        @Schema(description = "Ligne visée ; vide pour la DSM. Ignorée à la modification") Long ligne,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String texte) {
}
