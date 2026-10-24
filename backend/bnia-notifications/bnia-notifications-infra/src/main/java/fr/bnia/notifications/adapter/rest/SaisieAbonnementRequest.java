package fr.bnia.notifications.adapter.rest;

import fr.bnia.notifications.application.SaisieAbonnement;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SaisieAbonnement",
        description = "L'abonnement push que le navigateur a reçu de son service push (RG-NOT-01)")
public record SaisieAbonnementRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "endpoint, URL HTTPS") String adresse,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "keys.p256dh, en base64url") String cleP256dh,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "keys.auth, en base64url") String secretAuth) {

    SaisieAbonnement saisie() {
        return new SaisieAbonnement(adresse, cleP256dh, secretAuth);
    }
}
