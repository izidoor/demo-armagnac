package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.EtatDsm;
import fr.bnia.declarations.domain.TypeDeclarant;
import java.time.YearMonth;
import java.util.Optional;
import java.util.Set;

/**
 * Filtres du listing des DSM du BNIA (spec 05 §6.1, UC-DEC-7) ; chacun est facultatif.
 *
 * @param mois retient les DSM dont la période contient ce mois : le mois d'un négociant, l'année du 1er avril
 *             au 31 mars d'un producteur (décision du 2026-10-13)
 * @param etats retient les DSM dans l'un de ces états ; vide, tous les états (l'onglet « En cours » du
 *              listing réunit les quatre états non validés)
 */
public record FiltreDsm(Optional<Long> operateur, Optional<TypeDeclarant> type, Optional<YearMonth> mois,
        Set<EtatDsm> etats) {

    public FiltreDsm {
        etats = Set.copyOf(etats);
    }
}
