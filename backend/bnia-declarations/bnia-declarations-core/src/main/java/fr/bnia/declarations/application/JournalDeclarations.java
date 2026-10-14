package fr.bnia.declarations.application;

import java.util.List;

/** Journal des déclarations, hors agrégat, en ajout seul (RG-DEC-23, A-DEC-3). */
public interface JournalDeclarations {

    void ajouter(EntreeJournal entree);

    /** Du plus récent au plus ancien. */
    List<EntreeJournal> deLaDsm(long dsm);

    /** Du plus récent au plus ancien, brouillons supprimés compris. */
    List<EntreeJournal> deLOperateur(long operateur);

    /** RG-DEC-22 : les entrées d'un brouillon supprimé disparaissent avec lui. */
    void supprimerDeLaDsm(long dsm);
}
