package fr.bnia.authentification.application;

import java.util.List;

/** Journal des connexions, hors agrégat, en ajout seul (RG-AUT-12). */
public interface JournalConnexions {

    void ajouter(LigneConnexion ligne);

    /** Du plus récent au plus ancien, cinq cents lignes au plus. */
    List<LigneConnexion> lister(FiltreConnexions filtre);
}
