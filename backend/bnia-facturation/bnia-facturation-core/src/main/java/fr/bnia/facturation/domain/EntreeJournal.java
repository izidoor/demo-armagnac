package fr.bnia.facturation.domain;

import java.time.Instant;

/**
 * Entrée du journal d'une pièce (spec 08 §3.9), en ajout seul (RG-FAC-20). L'acteur est copié, sans clé
 * étrangère (ADR-011 amendé).
 *
 * @param utilisateur identifiant de l'utilisateur ; {@code null} pour un traitement
 * @param nomActeur   copie du nom de l'utilisateur, ou nom du traitement
 * @param detail      facultatif : texte d'une note, motif d'un rejet, numéro d'un avoir
 */
public record EntreeJournal(long piece, Instant date, EvenementJournal evenement, Long utilisateur, String nomActeur,
        CoteJournal cote, String detail) {

    public static EntreeJournal par(Auteur auteur, long piece, Instant date, EvenementJournal evenement,
            CoteJournal cote, String detail) {
        return new EntreeJournal(piece, date, evenement, auteur.utilisateur(), auteur.nom(), cote, detail);
    }
}
