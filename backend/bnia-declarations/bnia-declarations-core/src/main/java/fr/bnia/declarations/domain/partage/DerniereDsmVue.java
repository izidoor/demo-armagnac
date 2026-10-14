package fr.bnia.declarations.domain.partage;

import java.time.LocalDate;

/**
 * La DSM la plus récente d'un opérateur, quel que soit son état (spec 05 §5.2), pour la fiche opérateur.
 *
 * @param dsm identifiant de la DSM
 */
public record DerniereDsmVue(long dsm, long operateur, LocalDate debut, LocalDate fin, EtatDsm etat) {
}
