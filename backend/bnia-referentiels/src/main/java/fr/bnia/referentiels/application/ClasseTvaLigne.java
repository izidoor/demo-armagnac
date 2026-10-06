package fr.bnia.referentiels.application;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Ligne du listing des classes de TVA (modèle de lecture, ADR-004).
 *
 * @param tauxEnVigueur        taux applicable à la date du listing, {@code null} s'il n'y en a pas
 *                             (RG-REF-12)
 * @param debutTauxEnVigueur   date d'entrée en vigueur de ce taux, {@code null} s'il n'y en a pas
 */
public record ClasseTvaLigne(long id, String code, String libelle, boolean actif, BigDecimal tauxEnVigueur,
        LocalDate debutTauxEnVigueur) {
}
