package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;

/**
 * Numéro de TVA intracommunautaire français (RG-OPE-06, révision de la spec 08) : {@code FR}, une clé de
 * 2 chiffres, puis le SIREN ; clé = (12 + 3 × (SIREN modulo 97)) modulo 97. Les espaces saisis sont ignorés,
 * les lettres mises en majuscules.
 */
public record NumeroTva(String valeur) {

    public NumeroTva {
        valeur = valeur == null ? "" : Textes.compact(valeur);
        if (!valeur.matches("FR[0-9]{11}")) {
            throw new RegleEnfreinte("RG-OPE-06",
                    "N° de TVA invalide : « " + valeur + " » (FR, une clé de 2 chiffres, puis les 9 chiffres du SIREN).");
        }
        if (Integer.parseInt(valeur.substring(2, 4)) != cle(valeur.substring(4))) {
            throw new RegleEnfreinte("RG-OPE-06", "N° de TVA invalide : « " + valeur + " » (la clé ne correspond pas au SIREN).");
        }
    }

    /** {@code null} si la saisie est vide. */
    public static NumeroTva facultatif(String saisie) {
        String texte = Textes.facultatif(saisie);
        return texte == null ? null : new NumeroTva(texte);
    }

    /** Les 9 chiffres du SIREN. */
    public String siren() {
        return valeur.substring(4);
    }

    /** RG-OPE-06 : le SIRET commence par le SIREN de ce numéro ; sinon, la fiche avertit sans bloquer. */
    public boolean concordeAvec(Siret siret) {
        return siret.valeur().startsWith(siren());
    }

    private static int cle(String siren) {
        return (int) ((12 + 3 * (Long.parseLong(siren) % 97)) % 97);
    }

    @Override
    public String toString() {
        return valeur;
    }
}
