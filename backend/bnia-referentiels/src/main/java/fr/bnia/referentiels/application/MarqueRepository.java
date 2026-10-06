package fr.bnia.referentiels.application;

import fr.bnia.referentiels.domain.LibelleNormalise;
import fr.bnia.referentiels.domain.Marque;
import java.util.Optional;

public interface MarqueRepository extends ValeursRepository<Marque> {

    /** La marque, active ou non, dont le libellé a cette forme normalisée (RG-REF-15). */
    Optional<Marque> parLibelleNormalise(LibelleNormalise libelle);
}
