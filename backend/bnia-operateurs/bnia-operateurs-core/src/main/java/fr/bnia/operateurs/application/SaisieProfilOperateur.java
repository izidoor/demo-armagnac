package fr.bnia.operateurs.application;

import java.math.BigDecimal;

/** Saisie du profil par un opérateur (« Mon entreprise ») : les seuls champs d'identité qu'il modifie (RG-OPE-36). */
public record SaisieProfilOperateur(
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
        boolean accordDiffusionGeneral,
        Long version) {
}
