package fr.bnia.facturation.api;

import java.math.BigDecimal;

/**
 * Un article d'un catalogue, à une date (spec 08 §3.11) : ce que {@code facturation} recopie sur une ligne
 * (RG-FAC-03).
 *
 * @param prixUnitaire       prix unitaire HT à la date, 4 décimales
 * @param tva                catégorie de TVA à la date
 * @param codeComptable      facultatif ({@code null})
 * @param actif              un article inactif ne se facture pas (RG-FAC-03)
 * @param lienAdministration chemin de l'écran où la source administre l'article (RG-FAC-29)
 */
public record Article(String source, String code, String designation, String unite, BigDecimal prixUnitaire,
        CategorieTva tva, NatureArticle nature, String codeComptable, boolean actif, String lienAdministration) {

    public ArticleRef ref() {
        return new ArticleRef(source, code);
    }
}
