package fr.bnia.facturation.api;

import java.math.BigDecimal;
import java.util.List;

/** Une ligne d'une demande de facture (spec 08 §3.11), dans l'ordre d'impression. */
public sealed interface LigneDemandee {

    /**
     * Une ligne article, résolue par le catalogue de sa source (RG-FAC-03).
     *
     * @param quantite     non nulle, signée, 4 décimales (RG-FAC-06)
     * @param prixUnitaire facultatif ({@code null}) : sinon, celui de l'article
     * @param designation  facultative ({@code null}) : sinon, celle de l'article
     */
    record ArticleDemande(ArticleRef article, BigDecimal quantite, BigDecimal prixUnitaire, String designation)
            implements LigneDemandee {
    }

    /** Une ligne informative, imprimée telle quelle, sans montant. */
    record TexteDemande(String texte) implements LigneDemandee {
    }

    /** Un groupe titré, avec son sous-total ; ses lignes sont des articles ou des textes, jamais un groupe (RG-FAC-04). */
    record GroupeDemande(String titre, List<LigneDemandee> lignes) implements LigneDemandee {

        public GroupeDemande {
            lignes = List.copyOf(lignes);
        }
    }
}
