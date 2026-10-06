package fr.bnia.referentiels.adapter.rest;

import fr.bnia.referentiels.application.ValeurSimpleLigne;
import fr.bnia.referentiels.domain.ValeurSimple;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ValeurSimple", description = "Valeur d'une liste de référentiel")
public record ValeurSimpleResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Identifiant technique, référencé par les autres contextes", example = "1") long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "EUR") String code,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Europe") String libelle,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean actif) {

    static ValeurSimpleResponse depuis(ValeurSimple valeur) {
        return new ValeurSimpleResponse(valeur.id(), valeur.code().valeur(), valeur.libelle().valeur(),
                valeur.estActif());
    }

    static ValeurSimpleResponse depuis(ValeurSimpleLigne ligne) {
        return new ValeurSimpleResponse(ligne.id(), ligne.code(), ligne.libelle(), ligne.actif());
    }
}
