package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.PointDeDepartDeclaratif;
import java.util.List;
import java.util.Optional;

/** Port d'écriture de l'agrégat {@link PointDeDepartDeclaratif}. */
public interface PointDeDepartRepository {

    PointDeDepartDeclaratif inserer(PointDeDepartDeclaratif point);

    /** @throws fr.bnia.commun.domain.ModificationConcurrente s'il a changé depuis son chargement */
    PointDeDepartDeclaratif mettreAJour(PointDeDepartDeclaratif point);

    Optional<PointDeDepartDeclaratif> parId(long id);

    /** Le point de départ en vigueur de l'opérateur : le plus récent (spec 05 §3.3). */
    Optional<PointDeDepartDeclaratif> enVigueur(long operateur);

    /** Tous les points de départ de l'opérateur, du plus récent au plus ancien (RG-DEC-09). */
    List<PointDeDepartDeclaratif> deLOperateur(long operateur);
}
