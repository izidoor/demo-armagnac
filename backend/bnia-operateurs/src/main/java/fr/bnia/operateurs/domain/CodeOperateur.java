package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;

/**
 * Code opérateur BNIA (§4.3.5 p.21) : obligatoire (RG-OPE-02), unique sans distinction de casse
 * (RG-OPE-04), proposé à la création (RG-OPE-03) et toujours modifiable à la main. Les autres contextes
 * référencent l'opérateur par son identifiant technique (ADR-011).
 */
public record CodeOperateur(String valeur) {

    public CodeOperateur {
        if (valeur == null || valeur.isBlank()) {
            throw new RegleEnfreinte("RG-OPE-02", "Le code opérateur est obligatoire.");
        }
        valeur = valeur.strip();
    }

    public static CodeOperateur de(String valeur) {
        return new CodeOperateur(valeur);
    }

    @Override
    public String toString() {
        return valeur;
    }
}
