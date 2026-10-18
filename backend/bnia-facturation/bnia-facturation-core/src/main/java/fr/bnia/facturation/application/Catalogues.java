package fr.bnia.facturation.application;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.facturation.api.Article;
import fr.bnia.facturation.api.CatalogueArticles;
import fr.bnia.facturation.api.LigneDemandee.ArticleDemande;
import fr.bnia.facturation.domain.LigneFacture.LigneArticle;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tous les catalogues des sources, indexés par source (A-FAC-2) : deux catalogues de même source font échouer
 * le démarrage. Partagé par les cas d'usage du contexte.
 */
final class Catalogues {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Map<String, CatalogueArticles> parSource = new HashMap<>();

    Catalogues(List<CatalogueArticles> catalogues) {
        for (CatalogueArticles catalogue : catalogues) {
            if (parSource.putIfAbsent(catalogue.source(), catalogue) != null) {
                throw new IllegalStateException("Deux catalogues d'articles pour la source « " + catalogue.source()
                        + " » (A-FAC-2).");
            }
        }
    }

    /** Les articles actifs de tous les catalogues à cette date, par source puis par code. */
    List<Article> actifs(LocalDate date) {
        return tous(date).stream().filter(Article::actif).toList();
    }

    /** Les articles de tous les catalogues à cette date, actifs ou non, par source puis par code (RG-FAC-29). */
    List<Article> tous(LocalDate date) {
        return parSource.values().stream().flatMap(c -> c.articles(date).stream())
                .sorted(java.util.Comparator.comparing(Article::source).thenComparing(Article::code)).toList();
    }

    /**
     * RG-FAC-03 : l'article à la date de l'opération ; désignation, unité, nature, code comptable et catégorie de
     * TVA recopiés ; le prix de la demande s'il est donné, sinon celui de l'article. Inconnu ou inactif : refusé.
     */
    LigneArticle resoudre(ArticleDemande demande, LocalDate date) {
        String source = demande.article().source();
        String code = demande.article().code();
        CatalogueArticles catalogue = parSource.get(source);
        if (catalogue == null) {
            throw new RegleEnfreinte("RG-FAC-03", "Aucun catalogue d'articles pour la source « " + source + " ».");
        }
        Article article = catalogue.article(code, date).orElseThrow(() -> new RegleEnfreinte("RG-FAC-03",
                "Article inconnu au " + date.format(DATE) + " : « " + code + " » (" + source + ")."));
        if (!article.actif()) {
            throw new RegleEnfreinte("RG-FAC-03", "L'article « " + article.designation() + " » est inactif.");
        }
        return new LigneArticle(article.ref(),
                demande.designation() == null ? article.designation() : demande.designation(), article.unite(),
                article.nature(), article.codeComptable(), demande.quantite(),
                demande.prixUnitaire() == null ? article.prixUnitaire() : demande.prixUnitaire(), article.tva());
    }
}
