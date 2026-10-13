package fr.bnia.declarations.adapter.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SaisieCommentaireDsm", description = "Commentaire écrit ou modifié (RG-DEC-24, 25)")
public record CommentaireRequest(
        @Schema(description = "Ligne visée ; vide pour la DSM. Ignorée à la modification") Long ligne,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String texte,
        @Schema(description = "Interne au BNIA, invisible de l'opérateur ; côté BNIA seulement. Faux par défaut")
        Boolean interne) {

    boolean estInterne() {
        return Boolean.TRUE.equals(interne);
    }
}
