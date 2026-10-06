package fr.bnia.referentiels.domain;

import fr.bnia.commun.domain.RegleEnfreinte;

/**
 * Code métier d'une valeur de référentiel : obligatoire, sans espaces en tête ni en fin (RG-REF-01),
 * immuable (RG-REF-02). Les autres contextes y font référence (A-REF-1).
 */
public record Code(String valeur) {

    public Code {
        if (valeur == null || valeur.isBlank()) {
            throw new RegleEnfreinte("RG-REF-01", "Le code est obligatoire.");
        }
        valeur = valeur.strip();
    }

    public static Code de(String valeur) {
        return new Code(valeur);
    }

    @Override
    public String toString() {
        return valeur;
    }
}
