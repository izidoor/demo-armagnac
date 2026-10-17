package fr.bnia.facturation.api;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Une ligne vendue (spec 08 §3.11, RG-FAC-30, 31) : une ligne article d'une pièce émise, à sa date d'émission.
 * Celle d'un avoir compte en négatif : sa quantité et son HT sont de signe opposé à ceux de l'avoir.
 *
 * @param piece          identifiant de la pièce
 * @param avoir          vraie pour une ligne d'avoir
 * @param client         client de l'hôte de l'acheteur ; {@code null} pour un client libre
 * @param origine        origine de la facture ; {@code null} pour un avoir
 * @param codeComptable  facultatif ({@code null})
 * @param quantite       signée, 4 décimales
 * @param prixUnitaire   prix unitaire HT de la ligne, 4 décimales
 * @param montantHt      signé, 2 décimales
 */
public record LigneVendue(long piece, String numero, boolean avoir, LocalDate dateEmission, String acheteur,
        ClientRef client, Origine origine, ArticleRef article, String designation, String unite, NatureArticle nature,
        String codeComptable, CategorieTva tva, BigDecimal quantite, BigDecimal prixUnitaire, BigDecimal montantHt) {
}
