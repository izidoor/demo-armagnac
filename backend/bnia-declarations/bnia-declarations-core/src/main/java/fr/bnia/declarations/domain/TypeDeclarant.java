package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.partage.TypeOperateur;

/**
 * Type de déclarant d'une DSM (spec 05 §3.2) : lu sur l'opérateur à l'initiation, puis figé (RG-DEC-02).
 * Il décide de la période (RG-DEC-03) et des colonnes grisées (RG-DEC-16).
 */
public enum TypeDeclarant {

    PRODUCTEUR("Producteur"),
    NEGOCIANT("Négociant");

    private final String libelle;

    TypeDeclarant(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }

    /** RG-DEC-02 : seuls un producteur et un négociant déclarent. */
    public static TypeDeclarant de(TypeOperateur type) {
        return switch (type) {
            case PRODUCTEUR -> PRODUCTEUR;
            case NEGOCIANT -> NEGOCIANT;
            case AUTRE, BNIA -> throw new RegleEnfreinte("RG-DEC-02",
                    "Seul un producteur ou un négociant déclare ses stocks (type « " + type.libelle() + " »).");
        };
    }
}
