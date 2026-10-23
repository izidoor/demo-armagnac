package fr.bnia.declarations.application.partage;

import fr.bnia.declarations.domain.partage.DerniereDsmVue;
import fr.bnia.declarations.domain.partage.StockDeclareVue;
import fr.bnia.declarations.domain.partage.StockEstimeVue;
import java.util.Optional;

/**
 * Lecture des déclarations par les autres contextes (spec 05 §5.2, ADR-017), seule porte d'entrée avec les types
 * de {@code domain.partage}. Lecture seule, sans contrôle de droits : le contexte lecteur vérifie les siens.
 * Étendue par les specs consommatrices (décision du 2026-10-13).
 */
public interface DeclarationsLecture {

    /** La DSM la plus récente de l'opérateur, quel que soit son état, pour sa fiche ; vide s'il n'en a pas. */
    Optional<DerniereDsmVue> derniereDsm(long operateur);

    /** RG-DEC-42 : le stock déclaré de l'opérateur, pour sa fiche ; vide sans DSM validée. */
    Optional<StockDeclareVue> stockDeclare(long operateur);

    /**
     * RG-CA-11 (spec 06) : le stock estimé de l'opérateur, base vieillie à aujourd'hui et mouvements de contrat ;
     * sans ligne s'il n'a ni DSM validée ni point de départ.
     */
    StockEstimeVue stockEstime(long operateur);
}
