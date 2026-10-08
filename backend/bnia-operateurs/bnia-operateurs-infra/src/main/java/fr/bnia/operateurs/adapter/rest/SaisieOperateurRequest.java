package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.SaisieOperateur;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "SaisieOperateur",
        description = "Création d'un opérateur, ou modification de son identité (onglet Général) ; tout se corrige, code compris")
public record SaisieOperateurRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) TypeOperateur type,
        @Schema(description = "Absent à la création : le code proposé (RG-OPE-03) ; obligatoire en modification", example = "000012") String code,
        @Schema(description = "Comparé à la casse, aux accents et aux espaces près (RG-OPE-09)", example = "Maison Durand") String groupe,
        @Schema(example = "3203200123") String cvi,
        @Schema(example = "41234567800012") String siret,
        @Schema(example = "FR012345E6789") String accise,
        String bureauDouanes,
        @Schema(description = "Unique (RG-OPE-07)") String identifiantOdg,
        @Schema(description = "Identifiant du titre") Long titre,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Domaine Durand") String raisonSociale,
        String nomCommercial,
        String adresseLigne1,
        String adresseLigne2,
        String adresseLigne3,
        @Schema(example = "32100") String codePostal,
        @Schema(example = "Condom") String ville,
        @Schema(description = "Identifiant du pays ; absent à la création : la France (RG-OPE-02)") Long pays,
        String telephone1,
        String telephone2,
        String portable,
        @Schema(example = "contact@domaine-durand.fr") String emailGeneral,
        @Schema(example = "https://www.domaine-durand.fr") String siteWeb,
        String siteCommercial,
        @Schema(description = "Degrés décimaux, avec la longitude (RG-OPE-20)", example = "43.958") BigDecimal latitude,
        @Schema(description = "Degrés décimaux, avec la latitude (RG-OPE-20)", example = "0.372") BigDecimal longitude,
        @Schema(description = "Identifiant du membre BNIA") Long membreBnia,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean accordDiffusionGeneral,
        @Schema(description = "Masqué au portail (RG-OPE-36)") String commentaireInterne) {

    SaisieOperateur versSaisie() {
        return new SaisieOperateur(type, code, groupe, cvi, siret, accise, bureauDouanes, identifiantOdg, titre,
                raisonSociale, nomCommercial, adresseLigne1, adresseLigne2, adresseLigne3, codePostal, ville, pays,
                telephone1, telephone2, portable, emailGeneral, siteWeb, siteCommercial, latitude, longitude,
                membreBnia, accordDiffusionGeneral, commentaireInterne);
    }
}
