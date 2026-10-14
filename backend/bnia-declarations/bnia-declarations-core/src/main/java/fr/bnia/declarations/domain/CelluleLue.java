package fr.bnia.declarations.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Une cellule du modèle Excel, lue telle quelle par l'adapter (ADR-020) : le domaine la contrôle. Elle se situe
 * pour les erreurs : onglet, ligne et colonne du classeur (RG-DEC-37).
 *
 * @param ligne   numéro de ligne du classeur, à partir de 1
 * @param colonne lettre de colonne du classeur ({@code D})
 * @param texte   le contenu affiché, {@code null} si vide ; pour un nombre, son écriture décimale
 * @param nombre  la valeur d'une cellule numérique, {@code null} sinon
 */
public record CelluleLue(OngletDsm onglet, int ligne, String colonne, String texte, BigDecimal nombre) {

    public CelluleLue {
        Objects.requireNonNull(onglet, "onglet");
        Objects.requireNonNull(colonne, "colonne");
        texte = texte == null || texte.isBlank() ? null : texte.strip();
    }

    /** Vide, ou nombre nul : une rangée du modèle encore vierge. */
    public boolean vide() {
        return nombre != null ? nombre.signum() == 0 : texte == null;
    }

    /** Sans contenu : ni texte ni nombre (une formule jamais calculée par un tableur se lit ainsi). */
    boolean absente() {
        return texte == null && nombre == null;
    }

    /** Le nombre de la cellule, ou son texte s'il s'écrit comme un nombre ({@code 1234,5}) ; {@code null} sinon. */
    BigDecimal enNombre() {
        if (nombre != null) {
            return nombre;
        }
        if (texte == null) {
            return null;
        }
        try {
            return new BigDecimal(texte.replaceAll("[\\s\\u00a0\\u202f]", "").replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
