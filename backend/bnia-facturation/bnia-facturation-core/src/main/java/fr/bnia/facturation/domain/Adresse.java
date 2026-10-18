package fr.bnia.facturation.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Adresse d'un acheteur ou du vendeur (spec 08 §3.5, §3.7). Les textes vides valent « non renseigné ».
 *
 * @param pays     nom du pays, imprimé
 * @param codePays code ISO du pays, {@code FR} pour la France (RG-FAC-10) ; {@code null} s'il est inconnu
 */
public record Adresse(String ligne1, String ligne2, String codePostal, String ville, String pays, String codePays) {

    public Adresse {
        ligne1 = Textes.facultatif(ligne1);
        ligne2 = Textes.facultatif(ligne2);
        codePostal = Textes.facultatif(codePostal);
        ville = Textes.facultatif(ville);
        pays = Textes.facultatif(pays);
        codePays = Textes.facultatif(codePays);
    }

    /** RG-FAC-10 : ce qui manque pour qu'elle soit complète (ligne 1, code postal, ville, pays). */
    public List<String> manques() {
        List<String> manques = new ArrayList<>();
        if (ligne1 == null) {
            manques.add("adresse");
        }
        if (codePostal == null) {
            manques.add("code postal");
        }
        if (ville == null) {
            manques.add("ville");
        }
        if (pays == null) {
            manques.add("pays");
        }
        return manques;
    }

    public boolean enFrance() {
        return "FR".equalsIgnoreCase(codePays);
    }
}
