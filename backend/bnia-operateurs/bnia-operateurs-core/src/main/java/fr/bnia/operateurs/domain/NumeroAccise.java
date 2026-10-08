package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;

/**
 * Numéro d'accise (C.1.1.1 p.101) : {@code FR} suivi de 11 caractères alphanumériques (RG-OPE-06). Les
 * espaces saisis sont ignorés, les lettres mises en majuscules.
 */
public record NumeroAccise(String valeur) {

    public NumeroAccise {
        valeur = valeur == null ? "" : Textes.compact(valeur);
        if (!valeur.matches("FR[A-Z0-9]{11}")) {
            throw new RegleEnfreinte("RG-OPE-06",
                    "Numéro d'accise invalide : « " + valeur + " » (FR suivi de 11 lettres ou chiffres).");
        }
    }

    /** {@code null} si la saisie est vide. */
    public static NumeroAccise facultatif(String saisie) {
        String texte = Textes.facultatif(saisie);
        return texte == null ? null : new NumeroAccise(texte);
    }

    @Override
    public String toString() {
        return valeur;
    }
}
