package fr.bnia.operateurs.domain;

/**
 * Type de visite proposé (C.1.5 p.103, RG-OPE-17).
 *
 * @param typeVisite  identifiant du type de visite dans {@code referentiels}
 * @param commentaire facultatif
 */
public record VisiteProposee(long typeVisite, String commentaire) {

    public VisiteProposee {
        commentaire = Textes.facultatif(commentaire);
    }
}
