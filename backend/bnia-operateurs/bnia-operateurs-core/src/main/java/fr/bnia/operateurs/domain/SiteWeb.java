package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Site web ou site commercial (C.1.1.1 p.101) : une URL {@code http} ou {@code https} (RG-OPE-20).
 */
public record SiteWeb(String valeur) {

    public SiteWeb {
        String texte = valeur == null ? "" : valeur.strip();
        if (!valide(texte)) {
            throw new RegleEnfreinte("RG-OPE-20",
                    "Adresse de site invalide : « " + texte + " » (attendu : http://… ou https://…).");
        }
        valeur = texte;
    }

    /** {@code null} si la saisie est vide. */
    public static SiteWeb facultatif(String saisie) {
        String texte = Textes.facultatif(saisie);
        return texte == null ? null : new SiteWeb(texte);
    }

    private static boolean valide(String texte) {
        try {
            URI uri = new URI(texte);
            return ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return valeur;
    }
}
