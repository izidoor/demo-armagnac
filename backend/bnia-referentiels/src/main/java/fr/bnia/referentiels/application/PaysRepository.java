package fr.bnia.referentiels.application;

import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.CodeIso;
import fr.bnia.referentiels.domain.Pays;

public interface PaysRepository extends ValeursRepository<Pays> {

    /**
     * Vrai si un autre pays porte ce code ISO (RG-REF-06).
     *
     * @param sauf pays à ignorer, celui que l'on modifie ; {@code null} à la création
     */
    boolean existeCodeIso(CodeIso codeIso, Code sauf);
}
