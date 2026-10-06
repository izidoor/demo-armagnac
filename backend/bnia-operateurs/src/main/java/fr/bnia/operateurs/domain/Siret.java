package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;

/**
 * SIRET (C.1.1.1 p.101) : 14 chiffres, clé de Luhn (RG-OPE-06). Les espaces saisis sont ignorés.
 */
public record Siret(String valeur) {

    public Siret {
        valeur = valeur == null ? "" : Textes.compact(valeur);
        if (!valeur.matches("[0-9]{14}") || !luhn(valeur)) {
            throw new RegleEnfreinte("RG-OPE-06",
                    "SIRET invalide : « " + valeur + " » (14 chiffres, clé de contrôle comprise).");
        }
    }

    /** {@code null} si la saisie est vide. */
    public static Siret facultatif(String saisie) {
        String texte = Textes.facultatif(saisie);
        return texte == null ? null : new Siret(texte);
    }

    private static boolean luhn(String chiffres) {
        int somme = 0;
        for (int i = 0; i < chiffres.length(); i++) {
            int chiffre = chiffres.charAt(chiffres.length() - 1 - i) - '0';
            if (i % 2 == 1) {
                chiffre *= 2;
                if (chiffre > 9) {
                    chiffre -= 9;
                }
            }
            somme += chiffre;
        }
        return somme % 10 == 0;
    }

    @Override
    public String toString() {
        return valeur;
    }
}
