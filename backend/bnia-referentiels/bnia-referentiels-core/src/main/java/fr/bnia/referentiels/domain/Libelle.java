package fr.bnia.referentiels.domain;

import fr.bnia.commun.domain.RegleEnfreinte;

/**
 * Libellé d'une valeur de référentiel : obligatoire (RG-REF-03), sans espaces en tête ni en fin.
 */
public record Libelle(String valeur) {

    public Libelle {
        if (valeur == null || valeur.isBlank()) {
            throw new RegleEnfreinte("RG-REF-03", "Le libellé est obligatoire.");
        }
        valeur = valeur.strip();
    }

    public static Libelle de(String valeur) {
        return new Libelle(valeur);
    }

    @Override
    public String toString() {
        return valeur;
    }
}
