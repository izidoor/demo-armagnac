package fr.bnia.referentiels.domain;

import fr.bnia.commun.domain.RegleEnfreinte;

/**
 * Code d'une valeur de référentiel : obligatoire, sans espaces en tête ni en fin, unique dans sa liste
 * (RG-REF-01). Modifiable : les autres contextes référencent la valeur par son identifiant technique
 * (RG-REF-02, ADR-011).
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
