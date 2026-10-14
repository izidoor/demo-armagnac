package fr.bnia.declarations.application;

import fr.bnia.declarations.application.EditionsDsm.EditionDsm;
import fr.bnia.declarations.domain.ClasseurLu;

/**
 * Le modèle Excel de la DSM, la matrice négociant du BNIA (ADR-020, H-DEC-15, H-DEC-21), écrit et lu au même
 * format (adapter {@code document}) : ce que l'édition écrit, l'import le relit.
 */
public interface ClasseursDsm {

    /** RG-DEC-36 : la DSM au format du modèle, et un 6e onglet « Cotisation ». */
    byte[] ecrire(EditionDsm edition);

    /**
     * Le classeur lu tel quel, sans contrôle métier : {@link fr.bnia.declarations.domain.ImportExcel} s'en charge.
     *
     * @throws fr.bnia.commun.domain.RegleEnfreinte {@code RG-DEC-37}, si le fichier n'est pas un classeur au format
     *         du modèle (illisible, onglets ou numéros de colonne absents)
     */
    ClasseurLu lire(byte[] contenu);
}
