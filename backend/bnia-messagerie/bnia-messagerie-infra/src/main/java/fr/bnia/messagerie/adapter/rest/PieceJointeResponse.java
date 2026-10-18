package fr.bnia.messagerie.adapter.rest;

import fr.bnia.commun.domain.DocumentDepose;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PieceJointeMessage", description = "Pièce jointe d'un message de conversation libre (RG-MSG-14)")
public record PieceJointeResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Clé du document") long cle,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nom,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Type MIME") String type,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Taille en octets") long taille) {

    static PieceJointeResponse depuis(DocumentDepose d) {
        return new PieceJointeResponse(d.cle(), d.nom(), d.type(), d.taille());
    }
}
