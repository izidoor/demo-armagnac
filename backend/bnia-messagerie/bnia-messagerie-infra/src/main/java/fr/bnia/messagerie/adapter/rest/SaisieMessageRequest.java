package fr.bnia.messagerie.adapter.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SaisieMessage", description = "Message écrit ou modifié (RG-MSG-05, 06)")
public record SaisieMessageRequest(@Schema(requiredMode = Schema.RequiredMode.REQUIRED) String texte) {
}
