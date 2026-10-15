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
 * <p>
 * Chaque ligne a un numéro stable dans sa pièce, attribué à sa création ({@code 0} tant qu'elle n'est pas
 * rangée), et sait si elle a été reçue de la source avec la demande : une ligne reçue est verrouillée
 * (RG-FAC-17).
 */
public sealed interface LigneFacture {

    /** Numéro stable dans la pièce ; {@code 0} pour une ligne pas encore rangée. */
    int numero();

    /** Reçue de la source avec la demande (RG-FAC-17). */
    boolean recue();

    /** La même ligne, numérotée et marquée reçue ou non. */
    LigneFacture rangee(int numero, boolean recue);

    /** « la ligne « Part assujettie » », pour les messages. */
    String designation();

    /**
     * Une ligne article, recopiée de l'article de son catalogue (RG-FAC-03).
     *
     * @param codeComptable facultatif ({@code null})
     * @param quantite      non nulle, signée, 4 décimales (RG-FAC-06)
     * @param prixUnitaire  HT, signé, 4 décimales
     */
    record LigneArticle(ArticleRef article, String designation, String unite, NatureArticle nature,
            String codeComptable, BigDecimal quantite, BigDecimal prixUnitaire, CategorieTva tva, int numero,
            boolean recue) implements LigneFacture {

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

        /** Une ligne pas encore rangée dans une pièce. */
        public LigneArticle(ArticleRef article, String designation, String unite, NatureArticle nature,
                String codeComptable, BigDecimal quantite, BigDecimal prixUnitaire, CategorieTva tva) {
            this(article, designation, unite, nature, codeComptable, quantite, prixUnitaire, tva, 0, false);
        }

        /** RG-FAC-05 : quantité × prix unitaire, au centime, demi supérieur. */
        public BigDecimal montantHt() {
            return Montants.centimes(quantite.multiply(prixUnitaire));
        }

        @Override
        public LigneArticle rangee(int numero, boolean recue) {
            return new LigneArticle(article, designation, unite, nature, codeComptable, quantite, prixUnitaire, tva,
                    numero, recue);
        }

        /**
         * RG-FAC-17 : la ligne saisie dans un brouillon. Article, nature, code comptable et TVA restent ceux
         * enregistrés ; une ligne reçue ne change que de désignation.
         */
        public LigneArticle saisie(String designation, String unite, BigDecimal quantite, BigDecimal prixUnitaire) {
            return recue
                    ? new LigneArticle(article, designation, this.unite, nature, codeComptable, this.quantite,
                            this.prixUnitaire, tva, numero, true)
                    : new LigneArticle(article, designation, unite, nature, codeComptable, quantite, prixUnitaire,
                            tva, numero, false);
        }
    }

    /** Une ligne imprimée telle quelle, sans montant. */
    record LigneInformative(String texte, int numero, boolean recue) implements LigneFacture {

        public LigneInformative {
            texte = Textes.facultatif(texte);
            if (texte == null) {
                throw new RegleEnfreinte("RG-FAC-04", "Une ligne informative a un texte.");
            }
        }

        public LigneInformative(String texte) {
            this(texte, 0, false);
        }

        @Override
        public LigneInformative rangee(int numero, boolean recue) {
            return new LigneInformative(texte, numero, recue);
        }

        @Override
        public String designation() {
            return texte;
        }
    }

    /** RG-FAC-04 : un titre et des lignes article ou informatives, jamais un autre groupe. */
    record LigneGroupe(String titre, List<LigneFacture> lignes, int numero, boolean recue) implements LigneFacture {

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

        public LigneGroupe(String titre, List<LigneFacture> lignes) {
            this(titre, lignes, 0, false);
        }

        /** Le groupe numéroté ; ses lignes gardent les leurs. */
        @Override
        public LigneGroupe rangee(int numero, boolean recue) {
            return new LigneGroupe(titre, lignes, numero, recue);
        }

        /** Le même groupe, avec d'autres lignes. */
        public LigneGroupe avecLignes(List<LigneFacture> autres) {
            return new LigneGroupe(titre, autres, numero, recue);
        }

        @Override
        public String designation() {
            return titre;
        }

        /** RG-FAC-04 : somme des HT de ses lignes article. */
        public BigDecimal sousTotalHt() {
            return lignes.stream().filter(LigneArticle.class::isInstance).map(l -> ((LigneArticle) l).montantHt())
                    .reduce(Montants.ZERO, BigDecimal::add);
        }
    }
}
