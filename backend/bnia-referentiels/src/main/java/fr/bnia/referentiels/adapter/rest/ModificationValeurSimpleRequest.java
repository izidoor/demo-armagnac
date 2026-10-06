package fr.bnia.referentiels.adapter.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ModificationValeurSimple", description = "Le code est immuable (RG-REF-02) : seul le libellé change")
public record ModificationValeurSimpleRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Europe") String libelle) {
}
