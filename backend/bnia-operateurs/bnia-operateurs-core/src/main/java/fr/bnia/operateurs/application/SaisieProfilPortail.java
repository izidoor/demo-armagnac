package fr.bnia.operateurs.application;

import java.math.BigDecimal;

/** Saisie de « Mon entreprise » au portail : les seuls champs d'identité modifiables (RG-OPE-36). */
public record SaisieProfilPortail(
        Long titre,
        String nomCommercial,
        String adresseLigne1,
        String adresseLigne2,
        String adresseLigne3,
        String codePostal,
        String ville,
        Long pays,
        String telephone1,
        String telephone2,
        String portable,
        String emailGeneral,
        String siteWeb,
        String siteCommercial,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean accordDiffusionGeneral) {
}
