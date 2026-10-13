package fr.bnia.declarations.domain.partage;

import java.time.LocalDate;

/**
 * Une DSM vient d'être envoyée au BNIA, ou renvoyée après une demande de compléments (RG-DEC-20, ADR-013).
 * Sans écouteur tant que {@code notifications} n'existe pas.
 *
 * @param dsm       identifiant de la DSM
 * @param operateur identifiant de l'opérateur déclarant
 * @param debut     début de la période déclarée
 * @param fin       fin de la période déclarée
 * @param renvoi    {@code true} pour un renvoi après compléments
 */
public record DsmEnvoyee(long dsm, long operateur, LocalDate debut, LocalDate fin, boolean renvoi) {
}
