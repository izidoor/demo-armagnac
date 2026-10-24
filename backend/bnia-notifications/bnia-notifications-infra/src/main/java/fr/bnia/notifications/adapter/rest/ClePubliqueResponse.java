package fr.bnia.notifications.adapter.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ClePubliquePush", description = "La clé publique VAPID du serveur (RG-NOT-07)")
public record ClePubliqueResponse(
        @Schema(description = "En base64url ; vide si l'installation n'envoie pas de notification push") String clePublique) {
}
