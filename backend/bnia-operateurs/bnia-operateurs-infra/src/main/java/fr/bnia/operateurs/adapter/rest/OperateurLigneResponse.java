package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.OperateurLigne;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "OperateurLigne", description = "Ligne du listing des opérateurs")
public record OperateurLigneResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) TypeOperateur type,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "000012") String code,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String raisonSociale,
        String nomCommercial,
        String codePostal,
        String ville,
        String telephone1,
        String emailGeneral,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean actif,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Rien ne manque pour facturer (RG-OPE-02)") boolean facturable,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Rien ne manque pour certifier (RG-OPE-02)") boolean certifiable) {

    static OperateurLigneResponse depuis(OperateurLigne l) {
        return new OperateurLigneResponse(l.id(), l.type(), l.code(), l.raisonSociale(), l.nomCommercial(),
                l.codePostal(), l.ville(), l.telephone1(), l.emailGeneral(), l.actif(), l.facturable(),
                l.certifiable());
    }
}
