package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.domain.partage.ResultatRecherche;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ResultatRechercheOperateur", description = "Autre opérateur, vu du portail : rien d'autre que ces champs (RG-OPE-37)")
public record ResultatRechercheResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String code,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String raisonSociale,
        String nomCommercial,
        String ville) {

    static ResultatRechercheResponse depuis(ResultatRecherche r) {
        return new ResultatRechercheResponse(r.id(), r.code(), r.raisonSociale(), r.nomCommercial().orElse(null),
                r.ville().orElse(null));
    }
}
