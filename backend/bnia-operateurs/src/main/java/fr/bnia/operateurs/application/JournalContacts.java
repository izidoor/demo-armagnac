package fr.bnia.operateurs.application;

import java.util.List;

/** Journal des contacts, hors agrégat, en ajout seul (RG-OPE-34, ADR-012). */
public interface JournalContacts {

    void ajouter(long operateur, LigneJournal ligne);

    /** Du plus récent au plus ancien ; reste lisible après la suppression du contact. */
    List<LigneJournal> duContact(long operateur, long contact);
}
