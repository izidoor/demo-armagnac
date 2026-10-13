package fr.bnia.declarations.domain.partage;

import java.time.LocalDate;

/**
 * Le BNIA vient de valider une DSM (RG-DEC-20, ADR-013) : elle est figée, son stock de fin devient le stock
 * déclaré de l'opérateur. Sans écouteur tant que {@code notifications} n'existe pas.
 *
 * @param dsm       identifiant de la DSM
 * @param operateur identifiant de l'opérateur déclarant
 * @param debut     début de la période déclarée
 * @param fin       fin de la période déclarée
 */
public record DsmValidee(long dsm, long operateur, LocalDate debut, LocalDate fin) {
}
