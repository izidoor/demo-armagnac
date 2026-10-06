package fr.bnia.referentiels.application;

import java.math.BigDecimal;

/**
 * Ligne du listing des classes de TVA (modèle de lecture, ADR-004).
 *
 * @param tauxEnVigueur taux applicable à la date du listing, {@code null} s'il n'y en a pas (RG-REF-12)
 */
public record ClasseTvaLigne(String code, String libelle, boolean actif, BigDecimal tauxEnVigueur) {
}
