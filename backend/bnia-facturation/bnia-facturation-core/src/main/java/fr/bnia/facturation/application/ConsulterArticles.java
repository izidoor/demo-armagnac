package fr.bnia.facturation.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.facturation.api.Acteur;
import fr.bnia.facturation.api.Article;
import fr.bnia.facturation.api.CatalogueArticles;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** Le tableau de tous les articles de tous les catalogues (UC-FAC-6). RG-FAC-26, 29. */
@UseCase
public class ConsulterArticles {

    private final Catalogues catalogues;
    private final Controles controles;
    private final Clock horloge;

    public ConsulterArticles(List<CatalogueArticles> catalogues, DroitsFacturation droits, Clock horloge) {
        this.catalogues = new Catalogues(catalogues);
        this.controles = new Controles(droits);
        this.horloge = horloge;
    }

    /** RG-FAC-26, 29 : les articles de tous les catalogues à la date du jour, actifs ou non, par source et code. */
    public List<Article> articles(Acteur acteur) {
        controles.exigerLectureBnia(acteur);
        return catalogues.tous(LocalDate.now(horloge));
    }
}
