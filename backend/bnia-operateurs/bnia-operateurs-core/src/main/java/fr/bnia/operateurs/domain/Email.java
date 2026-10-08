package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Adresse e-mail : e-mail général de l'opérateur, adresse saisie du destinataire des factures
 * (RG-OPE-24), e-mail d'un contact (RG-OPE-28). Format contrôlé ; comparée sans distinction de casse.
 */
public record Email(String valeur) {

    private static final Pattern FORMAT = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

    /**
     * @param regle règle appliquée, pour le message d'erreur ({@code RG-OPE-24}, {@code RG-OPE-28})
     */
    public static Email de(String valeur, String regle) {
        String texte = valeur == null ? "" : valeur.strip();
        if (!FORMAT.matcher(texte).matches()) {
            throw new RegleEnfreinte(regle, "Adresse e-mail invalide : « " + texte + " ».");
        }
        return new Email(texte);
    }

    /** {@code null} si la saisie est vide. */
    public static Email facultatif(String saisie, String regle) {
        String texte = Textes.facultatif(saisie);
        return texte == null ? null : de(texte, regle);
    }

    /** Forme de comparaison : {@code Jeanne.Durand@Exemple.fr} et {@code jeanne.durand@exemple.fr} sont égales. */
    public String cle() {
        return valeur.toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return valeur;
    }
}
