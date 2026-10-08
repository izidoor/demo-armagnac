package fr.bnia.operateurs.domain;

/**
 * Label de l'opérateur (C.1.6 p.103, RG-OPE-17).
 *
 * @param label       identifiant du label dans {@code referentiels}
 * @param commentaire facultatif
 */
public record LabelObtenu(long label, String commentaire) {

    public LabelObtenu {
        commentaire = Textes.facultatif(commentaire);
    }
}
