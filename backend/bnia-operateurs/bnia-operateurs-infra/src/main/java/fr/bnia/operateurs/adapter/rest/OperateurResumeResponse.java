package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.OperateurResume;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "OperateurResume", description = "Désignation courte d'une autre fiche")
public record OperateurResumeResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "000007") String code,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Domaine du Tariquet") String raisonSociale,
        String ville,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean actif) {

    static OperateurResumeResponse depuis(OperateurResume r) {
        return new OperateurResumeResponse(r.id(), r.code(), r.raisonSociale(), r.ville(), r.actif());
    }
}
