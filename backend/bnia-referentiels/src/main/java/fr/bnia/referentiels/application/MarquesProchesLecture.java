package fr.bnia.referentiels.application;

import fr.bnia.referentiels.domain.LibelleNormalise;
import java.util.List;

/**
 * Marques dont le libellé normalisé contient un texte : suggestions pendant la saisie d'une marque
 * (RG-REF-15), en SQL direct (ADR-004).
 */
public interface MarquesProchesLecture {

    /** Marques actives et inactives, triées par libellé. */
    List<ValeurSimpleLigne> proches(LibelleNormalise texte, int nombreMaximum);
}
