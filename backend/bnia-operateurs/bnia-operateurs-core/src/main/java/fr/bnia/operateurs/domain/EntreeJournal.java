package fr.bnia.operateurs.domain;

import java.util.List;

/**
 * Fait à journaliser pour un contact (RG-OPE-34), produit par l'agrégat ; la date et l'auteur sont
 * ajoutés à l'enregistrement. Le nom est gardé : le journal survit à la suppression du contact.
 */
public record EntreeJournal(long contact, String nomContact, ActionContact action, List<Changement> changements) {

    public EntreeJournal {
        changements = List.copyOf(changements);
    }
}
