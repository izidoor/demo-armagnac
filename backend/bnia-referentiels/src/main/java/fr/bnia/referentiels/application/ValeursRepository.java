package fr.bnia.referentiels.application;

import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.ValeurSimple;
import java.util.Optional;

/**
 * Port d'écriture commun aux listes de référentiel. Chaque liste en dérive son propre port
 * (A-REF-3), implémenté dans {@code adapter.persistence}.
 */
public interface ValeursRepository<T extends ValeurSimple> {

    Optional<T> parCode(Code code);

    /** Vrai si une valeur porte ce code, sans distinction de casse (RG-REF-01). */
    boolean existeCode(Code code);

    void inserer(T valeur);

    void mettreAJour(T valeur);
}
