package fr.bnia.declarations.application;

import java.util.List;

/** Port de lecture des DSM (listings, spec 05 §3.6). */
public interface DsmLecture {

    /** Les DSM d'un opérateur, de la plus récente à la plus ancienne. */
    List<DsmLigne> deLOperateur(long operateur);

    /** UC-DEC-7 : les DSM de tous les opérateurs, filtrées, dans un ordre quelconque. */
    List<DsmLigne> rechercher(FiltreDsm filtre);

    /** H-DEC-8 : les dénominations déjà employées par l'opérateur, une par forme normalisée, triées. */
    List<String> denominations(long operateur);
}
