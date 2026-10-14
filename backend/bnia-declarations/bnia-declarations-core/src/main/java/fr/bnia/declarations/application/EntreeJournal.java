package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.ActionJournalDeclarations;
import java.time.Instant;

/**
 * Entrée du journal des déclarations (spec 05 §3.4, RG-DEC-23), telle qu'enregistrée. L'acteur et la DSM
 * sont copiés, sans clé étrangère : l'entrée « brouillon supprimé » survit à la DSM.
 *
 * @param acteur  identifiant de l'utilisateur
 * @param dsm     {@code null} pour une action sur le point de départ
 * @param periode copie du libellé de la période de la DSM, {@code null} sans DSM
 * @param detail  nom de fichier, mois, format… ; jamais de valeurs avant et après
 */
public record EntreeJournal(Instant date, long acteur, String nomActeur, long operateur, Long dsm, String periode,
        ActionJournalDeclarations action, String detail) {
}
