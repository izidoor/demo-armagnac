package fr.bnia.referentiels.application;

import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.ValeurSimple;
import java.util.Optional;

/**
 * Port d'écriture commun aux listes de référentiel. Chaque liste en dérive son propre port
 * (A-REF-3), implémenté dans {@code adapter.persistence}.
 */
public interface ValeursRepository<T extends ValeurSimple> {

    Optional<T> parId(long id);

    /**
     * Vrai si une autre valeur porte ce code, sans distinction de casse (RG-REF-01).
     *
     * @param sauf identifiant de la valeur modifiée, à ignorer ; {@code null} à la création
     */
    boolean existeCode(Code code, Long sauf);

    /** Enregistre une nouvelle valeur et la renvoie avec l'identifiant attribué par la base. */
    T inserer(T valeur);

    void mettreAJour(T valeur);

    /**
     * Supprime la valeur (RG-REF-04).
     *
     * @throws fr.bnia.commun.domain.ElementUtilise si d'autres données y font référence
     */
    void supprimer(long id);
}
