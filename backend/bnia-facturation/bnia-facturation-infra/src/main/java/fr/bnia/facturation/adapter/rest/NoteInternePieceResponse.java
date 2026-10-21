package fr.bnia.facturation.adapter.rest;

import fr.bnia.facturation.domain.NoteInterne;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "NoteInternePiece",
        description = "Note interne du BNIA sur une pièce, jamais imprimée, transmise ni montrée au client (RG-FAC-21)")
public record NoteInternePieceResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Copie du nom de l'auteur") String auteur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant ecriteLe,
        @Schema(description = "Renseigné si modifiée (« modifiée » affiché)") Instant modifieeLe,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String texte,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Écrite par l'utilisateur connecté : il la modifie ou la supprime") boolean deLUtilisateur) {

    static NoteInternePieceResponse depuis(NoteInterne n, long utilisateur) {
        return new NoteInternePieceResponse(n.id(), n.auteur().nom(), n.ecriteLe(), n.modifieeLe(), n.texte(),
                n.auteur().utilisateur() == utilisateur);
    }
}
