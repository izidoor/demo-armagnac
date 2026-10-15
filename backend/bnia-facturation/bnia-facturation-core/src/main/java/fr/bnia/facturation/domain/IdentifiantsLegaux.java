package fr.bnia.facturation.domain;

/**
 * Contrôle des identifiants légaux imprimés sur une pièce (spec 08 §3.5, §3.7, RG-FAC-10) : SIREN et SIRET par
 * la clé de Luhn, n° de TVA intracommunautaire français par sa clé.
 */
public final class IdentifiantsLegaux {

    private IdentifiantsLegaux() {
    }

    /** 9 chiffres, clé de Luhn juste. */
    public static boolean sirenValide(String siren) {
        return siren != null && siren.matches("[0-9]{9}") && luhn(siren);
    }

    /** 14 chiffres, clé de Luhn juste. */
    public static boolean siretValide(String siret) {
        return siret != null && siret.matches("[0-9]{14}") && luhn(siret);
    }

    /** {@code FR}, une clé de 2 chiffres, puis le SIREN ; clé = (12 + 3 × (SIREN modulo 97)) modulo 97. */
    public static boolean tvaValide(String numero) {
        if (numero == null || !numero.matches("FR[0-9]{11}")) {
            return false;
        }
        long siren = Long.parseLong(numero.substring(4));
        return Integer.parseInt(numero.substring(2, 4)) == (12 + 3 * (siren % 97)) % 97;
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
}
