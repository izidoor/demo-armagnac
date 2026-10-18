package fr.bnia.declarations.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.declarations.domain.GrilleCotisation;
import fr.bnia.declarations.domain.GrilleCotisation.TauxEnVigueur;
import fr.bnia.declarations.domain.PartCotisation;
import fr.bnia.declarations.domain.partage.LigneCotisation;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Le catalogue des articles de la cotisation (spec 08 §3.13) : deux articles par ligne de cotisation, la part
 * assujettie au taux de la classe de TVA de la CVO (H-DEC-22) et la part non assujettie, exonérée (H-FAC-1) ;
 * prix du taux CVO en vigueur à la date (H-FAC-2). Lu par {@code facturation} à travers l'infra, sans acteur :
 * les prix sont publics pour qui facture.
 */
@UseCase
public class ArticlesCotisation {

    private final GrilleCotisationRepository grilles;
    private final ReferentielsLecture referentiels;
    private final ParametresDeclarations parametres;

    /**
     * Un article de cotisation à une date.
     *
     * @param tauxTva taux de la part assujettie ; vide pour la part non assujettie, exonérée
     * @param actif   faux si aucun taux CVO, ou aucun taux de TVA pour la part assujettie, n'est en vigueur à la
     *                date : l'article ne se facture pas (RG-FAC-03)
     */
    public record ArticleCotisation(String code, String designation, BigDecimal prixUnitaire,
            Optional<BigDecimal> tauxTva, boolean actif) {
    }

    public ArticlesCotisation(GrilleCotisationRepository grilles, ReferentielsLecture referentiels,
            ParametresDeclarations parametres) {
        this.grilles = grilles;
        this.referentiels = referentiels;
        this.parametres = parametres;
    }

    /** Les huit articles à cette date, dans l'ordre des lignes de cotisation. */
    public List<ArticleCotisation> articles(LocalDate date) {
        GrilleCotisation grille = grilles.charger();
        Optional<BigDecimal> tva = tauxTvaCvo(date);
        List<ArticleCotisation> articles = new ArrayList<>();
        for (LigneCotisation ligne : LigneCotisation.values()) {
            for (PartCotisation part : PartCotisation.values()) {
                articles.add(article(new PartCotisation.Article(ligne, part), grille.tauxAu(date), tva));
            }
        }
        return articles;
    }

    /** L'article de ce code à cette date ; vide si le code n'est pas un article de cotisation. */
    public Optional<ArticleCotisation> article(String code, LocalDate date) {
        return PartCotisation.deCode(code)
                .map(a -> article(a, grilles.charger().tauxAu(date), tauxTvaCvo(date)));
    }

    private static ArticleCotisation article(PartCotisation.Article article, Optional<TauxEnVigueur> taux,
            Optional<BigDecimal> tva) {
        PartCotisation part = article.part();
        Optional<BigDecimal> tauxTva = part == PartCotisation.ASSUJETTIE ? tva : Optional.empty();
        boolean actif = taux.isPresent() && (part == PartCotisation.NON_ASSUJETTIE || tva.isPresent());
        return new ArticleCotisation(article.code(), part.designation(article.ligne()),
                taux.map(t -> part.prix(t.taux())).orElse(BigDecimal.ZERO.setScale(4)), tauxTva, actif);
    }

    /** H-DEC-22 : le taux de la classe de TVA de la CVO à la date ; vide s'il n'y en a pas. */
    private Optional<BigDecimal> tauxTvaCvo(LocalDate date) {
        return referentiels.classeTvaParCode(parametres.classeTvaCvo())
                .flatMap(classe -> referentiels.tauxTva(classe.id(), date))
                .map(t -> t.taux());
    }
}
