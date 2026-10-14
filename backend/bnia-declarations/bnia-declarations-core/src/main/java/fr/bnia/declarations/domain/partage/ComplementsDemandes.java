package fr.bnia.declarations.domain.partage;

import java.time.LocalDate;

/**
 * Le BNIA demande des compléments sur une DSM en instruction (RG-DEC-20, ADR-013) : elle revient à
 * l'opérateur. Sans écouteur tant que {@code notifications} n'existe pas.
 *
 * @param dsm       identifiant de la DSM
 * @param operateur identifiant de l'opérateur déclarant
 * @param debut     début de la période déclarée
 * @param fin       fin de la période déclarée
 * @param motif     le motif de la demande, commentaire de la DSM
 */
public record ComplementsDemandes(long dsm, long operateur, LocalDate debut, LocalDate fin, String motif) {
}
