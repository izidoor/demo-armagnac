package fr.bnia.operateurs.application;

import java.util.Set;

/**
 * Saisie d'un contact (C.1.9.1 p.104-105). Les références vers {@code referentiels} sont des identifiants.
 */
public record SaisieContact(
        Long titre,
        String nom,
        String prenom,
        String fonction,
        Set<Long> centresInteret,
        String telephoneProfessionnel,
        String telephoneSecondaire,
        String email,
        String adresse,
        String codePostal,
        String ville,
        Long pays,
        String observation) {
}
