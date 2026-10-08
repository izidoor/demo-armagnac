package fr.bnia.referentiels.application;

/**
 * Filtre d'un listing du back-office.
 *
 * @param actif     {@code null} : toutes les valeurs, actives ou non
 * @param recherche texte cherché dans le code ou le libellé, sans distinction de casse ;
 *                  {@code null} ou vide : aucun filtre
 */
public record FiltreValeurs(Boolean actif, String recherche) {

    public static final FiltreValeurs TOUTES = new FiltreValeurs(null, null);

    public FiltreValeurs {
        recherche = recherche == null || recherche.isBlank() ? null : recherche.strip();
    }
}
