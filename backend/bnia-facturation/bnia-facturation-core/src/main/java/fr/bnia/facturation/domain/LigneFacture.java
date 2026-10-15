package fr.bnia.facturation.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.facturation.api.ArticleRef;
import fr.bnia.facturation.api.CategorieTva;
import fr.bnia.facturation.api.NatureArticle;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Une ligne d'une pièce (spec 08 §3.2) : article, texte informatif, ou groupe titré à un seul niveau
 * (RG-FAC-04). Une ligne ne porte ni TVA ni TTC : la TVA ne se calcule qu'au récapitulatif (RG-FAC-05).
 */
public sealed interface LigneFacture {

    /**
     * Une ligne article, recopiée de l'article de son catalogue (RG-FAC-03).
     *
     * @param codeComptable facultatif ({@code null})
     * @param quantite      non nulle, signée, 4 décimales (RG-FAC-06)
     * @param prixUnitaire  HT, signé, 4 décimales
     */
    record LigneArticle(ArticleRef article, String designation, String unite, NatureArticle nature,
            String codeComptable, BigDecimal quantite, BigDecimal prixUnitaire, CategorieTva tva)
            implements LigneFacture {

        public LigneArticle {
            designation = Textes.facultatif(designation);
            if (designation == null) {
                throw new RegleEnfreinte("RG-FAC-03", "Une ligne article a une désignation.");
            }
            if (quantite == null || quantite.signum() == 0) {
                throw new RegleEnfreinte("RG-FAC-06", "La quantité de la ligne « " + designation + " » n'est jamais nulle.");
            }
            if (prixUnitaire == null || tva == null || nature == null) {
                throw new RegleEnfreinte("RG-FAC-03", "La ligne « " + designation + " » a un prix, une TVA et une nature.");
            }
            unite = Textes.facultatif(unite);
            codeComptable = Textes.facultatif(codeComptable);
            quantite = quantite.setScale(4, RoundingMode.HALF_UP);
            prixUnitaire = prixUnitaire.setScale(4, RoundingMode.HALF_UP);
        }

        /** RG-FAC-05 : quantité × prix unitaire, au centime, demi supérieur. */
        public BigDecimal montantHt() {
            return Montants.centimes(quantite.multiply(prixUnitaire));
        }
    }

    /** Une ligne imprimée telle quelle, sans montant. */
    record LigneInformative(String texte) implements LigneFacture {

        public LigneInformative {
            texte = Textes.facultatif(texte);
            if (texte == null) {
                throw new RegleEnfreinte("RG-FAC-04", "Une ligne informative a un texte.");
            }
        }
    }

    /** RG-FAC-04 : un titre et des lignes article ou informatives, jamais un autre groupe. */
    record LigneGroupe(String titre, List<LigneFacture> lignes) implements LigneFacture {

        public LigneGroupe {
            titre = Textes.facultatif(titre);
            if (titre == null) {
                throw new RegleEnfreinte("RG-FAC-04", "Un groupe de lignes a un titre.");
            }
            lignes = List.copyOf(lignes);
            if (lignes.stream().anyMatch(LigneGroupe.class::isInstance)) {
                throw new RegleEnfreinte("RG-FAC-04", "Le groupe « " + titre + " » ne contient pas d'autre groupe.");
            }
        }

        /** RG-FAC-04 : somme des HT de ses lignes article. */
        public BigDecimal sousTotalHt() {
            return lignes.stream().filter(LigneArticle.class::isInstance).map(l -> ((LigneArticle) l).montantHt())
                    .reduce(Montants.ZERO, BigDecimal::add);
        }
    }
}
