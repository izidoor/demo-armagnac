package fr.bnia.facturation.application;

import java.util.List;

/** Port de lecture des pièces (historique, RG-FAC-27). */
public interface PiecesLecture {

    /** Les pièces filtrées : émises, de la plus récente à la plus ancienne ; brouillons, par date de création. */
    List<PieceLigne> rechercher(FiltrePieces filtre);
}
