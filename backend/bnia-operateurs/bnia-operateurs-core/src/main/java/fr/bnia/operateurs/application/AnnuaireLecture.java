package fr.bnia.operateurs.application;

import java.util.List;

/** Lecture de l'annuaire d'un opérateur, sans copie de données (RG-OPE-52, vision §2.2, ADR-004). */
public interface AnnuaireLecture {

    /** Contacts, membres et invitations en attente de personnes nouvelles, triés par nom puis prénom. */
    List<PersonneAnnuaire> annuaire(long operateur);
}
