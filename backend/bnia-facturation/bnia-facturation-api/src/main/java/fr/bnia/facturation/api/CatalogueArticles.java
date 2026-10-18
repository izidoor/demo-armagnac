package fr.bnia.facturation.api;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Les articles d'une source, avec leur prix et leur TVA à une date (spec 08 §3.11, A-FAC-2) : implémenté dans
 * l'infra de chaque source ; {@code facturation} reçoit tous les catalogues, indexés par {@link #source()}.
 */
public interface CatalogueArticles {

    /** Nom de la source, unique : deux catalogues de même source font échouer le démarrage (A-FAC-2). */
    String source();

    /** Les articles à cette date, actifs ou non. */
    List<Article> articles(LocalDate date);

    /** L'article de ce code à cette date (H-FAC-2 : la date de l'opération) ; vide s'il est inconnu. */
    Optional<Article> article(String code, LocalDate date);
}
