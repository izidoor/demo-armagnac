package fr.bnia.referentiels.adapter.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CreationValeurSimple")
public record CreationValeurSimpleRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "EUR") String code,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Europe") String libelle) {
}
