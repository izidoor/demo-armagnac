package fr.bnia.referentiels.application;

import fr.bnia.referentiels.domain.CodeIso;
import fr.bnia.referentiels.domain.Pays;

public interface PaysRepository extends ValeursRepository<Pays> {

    /**
     * Vrai si un autre pays porte ce code ISO (RG-REF-06).
     *
     * @param sauf identifiant du pays modifié, à ignorer ; {@code null} à la création
     */
    boolean existeCodeIso(CodeIso codeIso, Long sauf);
}
