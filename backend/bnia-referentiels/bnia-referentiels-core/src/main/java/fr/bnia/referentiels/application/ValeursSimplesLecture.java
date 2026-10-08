package fr.bnia.referentiels.application;

import fr.bnia.referentiels.domain.ValeurSimple;
import java.util.List;

/**
 * Port de lecture des listings du back-office, en SQL direct (ADR-004). Le paramètre de type désigne
 * la liste lue ({@code ValeursSimplesLecture<Titre>}) ; Spring injecte l'implémentation par type
 * générique.
 */
public interface ValeursSimplesLecture<T extends ValeurSimple> {

    /** Valeurs triées par libellé. */
    List<ValeurSimpleLigne> lister(FiltreValeurs filtre);
}
