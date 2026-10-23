package fr.bnia.contratsarmagnac.application;

import fr.bnia.contratsarmagnac.domain.ContratAchatArmagnac;
import java.util.Optional;

/** Port d'écriture de l'agrégat {@link ContratAchatArmagnac}. */
public interface ContratRepository {

    ContratAchatArmagnac inserer(ContratAchatArmagnac contrat);

    /** @throws fr.bnia.commun.domain.ModificationConcurrente s'il a changé depuis son chargement */
    ContratAchatArmagnac mettreAJour(ContratAchatArmagnac contrat);

    Optional<ContratAchatArmagnac> parId(long id);
}
