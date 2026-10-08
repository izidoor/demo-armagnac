package fr.bnia.operateurs.application;

import java.util.List;
import java.util.Optional;

/** Lectures des utilisateurs (ADR-004) : liste du back-office (RG-OPE-53), « Mon compte » (RG-OPE-49). */
public interface UtilisateursLecture {

    /** Triés par nom, puis prénom. */
    List<UtilisateurLigne> lister(FiltreUtilisateurs filtre);

    Optional<UtilisateurLigne> utilisateur(long id);
}
