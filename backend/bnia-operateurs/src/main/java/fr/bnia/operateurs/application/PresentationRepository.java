package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.PresentationOperateur;
import java.util.Optional;

/** Agrégat {@code PresentationOperateur}, de même identifiant que l'opérateur (ADR-012). */
public interface PresentationRepository {

    /** Rien si l'opérateur n'a encore rien saisi. */
    Optional<PresentationOperateur> parId(long operateur);

    /** Crée la présentation au premier enregistrement, la remplace ensuite. */
    void enregistrer(PresentationOperateur presentation);
}
