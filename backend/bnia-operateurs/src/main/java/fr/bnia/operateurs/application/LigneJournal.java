package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.ActionContact;
import fr.bnia.operateurs.domain.Changement;
import java.time.Instant;
import java.util.List;

/**
 * Entrée du journal d'un contact, telle qu'enregistrée (RG-OPE-34). Les valeurs des références sont des
 * libellés.
 *
 * @param auteur « BNIA », « Portail » ou « Mon compte », tant que {@code acces} n'identifie pas la personne
 */
public record LigneJournal(
        Instant date,
        String auteur,
        long contact,
        String nomContact,
        ActionContact action,
        List<Changement> changements) {
}
