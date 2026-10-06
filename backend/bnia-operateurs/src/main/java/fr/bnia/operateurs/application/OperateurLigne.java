package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.partage.TypeOperateur;

/** Ligne du listing du back-office (spec 02, §6.1). */
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
        boolean incomplet) {
}
