package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.math.BigDecimal;

/**
 * Saisie de l'identité d'un opérateur, telle qu'elle arrive de l'écran : à la création, puis à la
 * modification de l'onglet Général. Les références vers {@code referentiels} sont des identifiants.
 *
 * @param code {@code null} ou vide à la création : le code proposé est attribué (RG-OPE-03)
 * @param pays {@code null} à la création : la France (RG-OPE-02)
 * @param version version lue par l'écran, {@code null} à la création (RG-OPE-51)
 */
public record SaisieOperateur(
        TypeOperateur type,
        String code,
        String groupe,
        String cvi,
        String siret,
        String numeroTva,
        String accise,
        String bureauDouanes,
        String identifiantOdg,
        Long titre,
        String raisonSociale,
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
        Long membreBnia,
        boolean accordDiffusionGeneral,
        String commentaireInterne,
        Long version) {
}
