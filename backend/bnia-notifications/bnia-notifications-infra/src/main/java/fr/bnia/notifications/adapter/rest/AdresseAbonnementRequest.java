package fr.bnia.notifications.adapter.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AdresseAbonnement", description = "L'adresse de l'abonnement push d'un appareil")
public record AdresseAbonnementRequest(@Schema(requiredMode = Schema.RequiredMode.REQUIRED) String adresse) {
}
