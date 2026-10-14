package fr.bnia.declarations.domain.partage;

import java.time.LocalDate;

/**
 * La période attendue d'un opérateur est en retard (RG-DEC-40, 41, ADR-013) : publié une seule fois par période,
 * par le traitement planifié quotidien, le lendemain de l'échéance. Sans écouteur tant que {@code notifications}
 * n'existe pas.
 *
 * @param operateur identifiant de l'opérateur attendu
 * @param debut     début de la période attendue
 * @param fin       fin de la période attendue
 * @param echeance  échéance passée (RG-DEC-39)
 * @param dsm       la DSM de la période, en brouillon ou en compléments demandés ; vide si elle est absente
 */
public record DeclarationEnRetard(long operateur, LocalDate debut, LocalDate fin, LocalDate echeance, Long dsm) {
}
