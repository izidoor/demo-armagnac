package fr.bnia.contratsarmagnac.application;

import fr.bnia.contratsarmagnac.domain.ActionContrat;
import fr.bnia.contratsarmagnac.domain.Partie;
import java.time.Instant;

/**
 * Une entrée du journal du contrat (spec 06 §3.5).
 *
 * @param detail numéro de version ou de retiraison ; {@code null} sans objet
 */
public record EntreeJournal(Instant date, Partie partie, long utilisateur, String nom, ActionContrat action,
        Integer detail) {
}
