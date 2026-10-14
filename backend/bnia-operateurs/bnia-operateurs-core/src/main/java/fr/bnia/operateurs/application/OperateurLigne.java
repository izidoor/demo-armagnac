package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.partage.TypeOperateur;

/**
 * Ligne du listing du back-office (spec 02, §6.1).
 *
 * @param facturable  RG-OPE-02 : rien ne manque pour facturer
 * @param certifiable RG-OPE-02 : rien ne manque pour certifier
 */
public record OperateurLigne(
        long id,
        TypeOperateur type,
        String code,
        String raisonSociale,
        String nomCommercial,
        String codePostal,
        String ville,
        String telephone1,
        String emailGeneral,
        boolean actif,
        boolean facturable,
        boolean certifiable) {
}
