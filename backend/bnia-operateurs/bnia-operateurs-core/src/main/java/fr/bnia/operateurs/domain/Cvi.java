package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;

/**
 * Numéro CVI (casier viticole informatisé, C.1.1.1 p.101) : 10 caractères alphanumériques
 * (RG-OPE-06). Les espaces saisis sont ignorés, les lettres mises en majuscules.
 */
public record Cvi(String valeur) {

    public Cvi {
        valeur = valeur == null ? "" : Textes.compact(valeur);
        if (!valeur.matches("[A-Z0-9]{10}")) {
            throw new RegleEnfreinte("RG-OPE-06", "CVI invalide : « " + valeur + " » (10 caractères, lettres ou chiffres).");
        }
    }

    /** {@code null} si la saisie est vide. */
    public static Cvi facultatif(String saisie) {
        String texte = Textes.facultatif(saisie);
        return texte == null ? null : new Cvi(texte);
    }

    @Override
    public String toString() {
        return valeur;
    }
}
