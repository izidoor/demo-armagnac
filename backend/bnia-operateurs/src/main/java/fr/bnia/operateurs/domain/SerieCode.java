package fr.bnia.operateurs.domain;

import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Pattern;

/**
 * Série de numérotation des codes opérateur (RG-OPE-03, §4.3.5 p.21) :
 * <ul>
 *   <li>producteur et {@code AUTRE} : six chiffres, une seule série ({@code 000001}) ;</li>
 *   <li>négociant : {@code N}, le département tiré du code postal français, puis quatre chiffres
 *       ({@code N320001}, {@code N2A0001}, {@code N9710001}).</li>
 * </ul>
 * Le numéro suivant est le plus grand numéro existant de la série, plus un (A-OPE-5).
 *
 * @param prefixe {@code ""} ou {@code N} suivi du département
 * @param largeur nombre de chiffres du numéro
 */
public record SerieCode(String prefixe, int largeur) {

    private static final Pattern CODE_POSTAL = Pattern.compile("[0-9]{5}");

    /**
     * La série d'un opérateur, ou rien si aucun code ne peut être proposé : négociant sans code postal
     * français.
     *
     * @param enFrance vrai si le pays de l'adresse est la France
     */
    public static Optional<SerieCode> pour(TypeOperateur type, String codePostal, boolean enFrance) {
        if (type == null) {
            return Optional.empty();
        }
        return switch (type) {
            case PRODUCTEUR, AUTRE -> Optional.of(new SerieCode("", 6));
            case NEGOCIANT -> enFrance ? departement(codePostal).map(d -> new SerieCode("N" + d, 4)) : Optional.empty();
        };
    }

    /**
     * Département d'un code postal français : les deux premiers chiffres ; {@code 2A} de 20000 à 20199
     * et {@code 2B} de 20200 à 20699 ; trois chiffres de 971 à 976.
     */
    static Optional<String> departement(String codePostal) {
        String cp = codePostal == null ? "" : codePostal.strip();
        if (!CODE_POSTAL.matcher(cp).matches()) {
            return Optional.empty();
        }
        int valeur = Integer.parseInt(cp);
        if (cp.startsWith("20")) {
            if (valeur <= 20199) {
                return Optional.of("2A");
            }
            return valeur <= 20699 ? Optional.of("2B") : Optional.empty();
        }
        if (cp.startsWith("97") && cp.charAt(2) >= '1' && cp.charAt(2) <= '6') {
            return Optional.of(cp.substring(0, 3));
        }
        return Optional.of(cp.substring(0, 2));
    }

    /** Le code suivant le plus grand numéro de la série ; rien si la série est pleine. */
    public Optional<CodeOperateur> suivant(OptionalInt plusGrandNumero) {
        int numero = plusGrandNumero.orElse(0) + 1;
        String chiffres = String.format("%0" + largeur + "d", numero);
        return chiffres.length() > largeur ? Optional.empty() : Optional.of(CodeOperateur.de(prefixe + chiffres));
    }

    /** Vrai si le code a la forme de la série, à la casse près (RG-OPE-05). */
    public boolean contient(CodeOperateur code) {
        return code.valeur().toUpperCase(Locale.ROOT).matches(motif());
    }

    /** Expression régulière des codes de la série, utilisable aussi en SQL ({@code ~}). */
    public String motif() {
        return "^" + prefixe + "[0-9]{" + largeur + "}$";
    }
}
