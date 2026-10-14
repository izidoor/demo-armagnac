package fr.bnia.declarations.application.partage;

import fr.bnia.declarations.domain.partage.CotisationDsmVue;
import java.util.Optional;

/**
 * Lecture des déclarations par les autres contextes (spec 05 §5.2, ADR-017), seule porte d'entrée avec les types
 * de {@code domain.partage}. Lecture seule, sans contrôle de droits : le contexte lecteur vérifie les siens.
 * Étendue par les specs consommatrices (décision du 2026-10-13).
 */
public interface DeclarationsLecture {

    /** La cotisation d'une DSM, pour {@code facturation} (RG-DEC-31 à 33) ; vide si la DSM n'existe pas. */
    Optional<CotisationDsmVue> cotisation(long dsm);
}
