package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.TypeCible;
import java.util.List;

/** Journal des personnes et des accès, hors agrégat, en ajout seul (RG-OPE-34, ADR-016). */
public interface JournalActions {

    void ajouter(LigneJournal ligne);

    /** Du plus récent au plus ancien. */
    List<LigneJournal> deLOperateur(long operateur);

    /** Du plus récent au plus ancien ; reste lisible après la suppression de la cible. */
    List<LigneJournal> deLaCible(TypeCible cible, long id);
}
