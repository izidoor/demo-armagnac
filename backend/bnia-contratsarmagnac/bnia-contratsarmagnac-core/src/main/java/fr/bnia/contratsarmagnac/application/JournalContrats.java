package fr.bnia.contratsarmagnac.application;

import fr.bnia.contratsarmagnac.domain.Fait;
import java.util.List;

/** Journal du contrat (RG-CA-19, spec 06 §3.5), en ajout seul. */
public interface JournalContrats {

    void ajouter(long contrat, Fait fait);

    /** Les entrées du contrat, de la plus ancienne à la plus récente. */
    List<EntreeJournal> duContrat(long contrat);
}
