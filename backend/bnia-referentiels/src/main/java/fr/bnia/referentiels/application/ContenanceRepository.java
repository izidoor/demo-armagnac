package fr.bnia.referentiels.application;

import fr.bnia.referentiels.domain.Contenance;
import fr.bnia.referentiels.domain.Volume;
import java.util.Optional;

/** Contenances, référencées par leur identifiant ; le volume est unique (RG-REF-10). */
public interface ContenanceRepository {

    Optional<Contenance> parId(long id);

    /**
     * Vrai si une autre contenance a ce volume (RG-REF-10).
     *
     * @param sauf identifiant de la contenance modifiée, à ignorer ; {@code null} à la création
     */
    boolean existeVolume(Volume volume, Long sauf);

    /** Enregistre une nouvelle contenance et la renvoie avec l'identifiant attribué par la base. */
    Contenance inserer(Contenance contenance);

    void mettreAJour(Contenance contenance);

    /** @throws fr.bnia.commun.domain.ElementUtilise si d'autres données y font référence */
    void supprimer(long id);
}
