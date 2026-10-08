package fr.bnia.referentiels.adapter.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SaisieValeurSimple", description = "Création ou modification : le code se corrige (RG-REF-02)")
public record SaisieValeurSimpleRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "EUR") String code,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Europe") String libelle) {
}
