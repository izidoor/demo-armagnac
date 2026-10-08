package fr.bnia.operateurs.application;

import java.util.Set;

/**
 * Déclaration d'un contact (vision UC-10, C.1.9.1 p.104-105) : son identité et le contenu de sa fiche
 * professionnelle. Les références vers {@code referentiels} sont des identifiants.
 */
public record SaisieContact(
        Long titre,
        String nom,
        String prenom,
        String email,
        String fonction,
        String telephoneProfessionnel,
        String telephoneSecondaire,
        String adresse,
        String codePostal,
        String ville,
        Long pays,
        Set<Long> centresInteret,
        String observation) {
}
