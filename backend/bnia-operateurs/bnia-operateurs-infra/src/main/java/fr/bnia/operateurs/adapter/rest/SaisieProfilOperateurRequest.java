package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.SaisieProfilOperateur;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "SaisieProfilOperateur",
        description = "« Mon entreprise » : les champs qu'un opérateur modifie lui-même, par le module Contacts (RG-OPE-36)")
public record SaisieProfilOperateurRequest(
        @Schema(description = "Identifiant du titre") Long titre,
        String nomCommercial,
        String adresseLigne1,
        String adresseLigne2,
        String adresseLigne3,
        String codePostal,
        String ville,
        @Schema(description = "Identifiant du pays") Long pays,
        String telephone1,
        String telephone2,
        String portable,
        String emailGeneral,
        String siteWeb,
        String siteCommercial,
        BigDecimal latitude,
        BigDecimal longitude,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean accordDiffusionGeneral,
        @Schema(description = "Version lue (RG-OPE-51)") Long version) {

    SaisieProfilOperateur versSaisie() {
        return new SaisieProfilOperateur(titre, nomCommercial, adresseLigne1, adresseLigne2, adresseLigne3, codePostal,
                ville, pays, telephone1, telephone2, portable, emailGeneral, siteWeb, siteCommercial, latitude, longitude,
                accordDiffusionGeneral, version);
    }
}
