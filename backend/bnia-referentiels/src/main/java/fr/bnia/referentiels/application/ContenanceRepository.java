package fr.bnia.referentiels.application;

import fr.bnia.referentiels.domain.Contenance;
import fr.bnia.referentiels.domain.Volume;
import java.util.Optional;

/** La contenance n'a pas de code : son volume l'identifie (RG-REF-01, RG-REF-10). */
public interface ContenanceRepository {

    Optional<Contenance> parVolume(Volume volume);

    boolean existeVolume(Volume volume);

    void inserer(Contenance contenance);

    void mettreAJour(Contenance contenance);
}
