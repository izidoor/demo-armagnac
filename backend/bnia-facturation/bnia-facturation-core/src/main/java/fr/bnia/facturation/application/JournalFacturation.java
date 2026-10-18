package fr.bnia.facturation.application;

import fr.bnia.facturation.domain.EntreeJournal;
import java.util.List;

/** Journal des pièces, hors agrégat, en ajout seul (spec 08 §3.9, RG-FAC-20). */
public interface JournalFacturation {

    void ajouter(EntreeJournal entree);

    /** Du plus récent au plus ancien. */
    List<EntreeJournal> deLaPiece(long piece);
}
