package fr.bnia.declarations.adapter.integration;

import fr.bnia.declarations.application.ArticlesCotisation;
import fr.bnia.declarations.application.ArticlesCotisation.ArticleCotisation;
import fr.bnia.facturation.api.Article;
import fr.bnia.facturation.api.CatalogueArticles;
import fr.bnia.facturation.api.CategorieTva;
import fr.bnia.facturation.api.NatureArticle;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Le catalogue de la cotisation, publié à {@code facturation} (A-FAC-2, spec 08 §3.13) : deux articles par ligne
 * de cotisation, en hlAP, des services ; la part non assujettie est exonérée (H-FAC-1). Administrés par la grille
 * CVO du Paramétrage.
 */
@Component
class CatalogueCotisation implements CatalogueArticles {

    /** H-FAC-1, Q-FAC-1 : motif d'exonération imprimé au récapitulatif de TVA. */
    static final String MOTIF_EXONERATION = "Part de la cotisation interprofessionnelle non assujettie à la TVA";
    private static final String GRILLE = "/bo/parametrage/taux-cvo";

    private final ArticlesCotisation articles;

    CatalogueCotisation(ArticlesCotisation articles) {
        this.articles = articles;
    }

    @Override
    public String source() {
        return FacturationAdapter.SOURCE;
    }

    @Override
    public List<Article> articles(LocalDate date) {
        return articles.articles(date).stream().map(CatalogueCotisation::article).toList();
    }

    @Override
    public Optional<Article> article(String code, LocalDate date) {
        return articles.article(code, date).map(CatalogueCotisation::article);
    }

    private static Article article(ArticleCotisation a) {
        // Sans taux de TVA à la date, l'article est inactif : une catégorie au taux le plus neutre le décrit.
        CategorieTva tva = a.tauxTva().filter(t -> t.signum() > 0).map(CategorieTva::normale)
                .orElseGet(() -> CategorieTva.exoneree(MOTIF_EXONERATION));
        return new Article(FacturationAdapter.SOURCE, a.code(), a.designation(), "hlAP", a.prixUnitaire(), tva,
                NatureArticle.SERVICE, null, a.actif(), GRILLE);
    }
}
