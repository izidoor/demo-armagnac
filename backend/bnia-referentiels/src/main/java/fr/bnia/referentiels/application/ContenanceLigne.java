package fr.bnia.referentiels.application;

import java.math.BigDecimal;

/**
 * Ligne du listing des contenances (modèle de lecture, ADR-004).
 *
 * @param libelle      le libellé saisi, sinon celui dérivé du volume (« 0,70 L »)
 * @param libelleSaisi {@code null} si aucun libellé n'a été saisi
 */
public record ContenanceLigne(BigDecimal volume, String libelle, String libelleSaisi, boolean actif) {
}
