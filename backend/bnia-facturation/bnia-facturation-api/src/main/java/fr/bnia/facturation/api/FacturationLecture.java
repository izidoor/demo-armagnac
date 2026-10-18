package fr.bnia.facturation.api;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * La lecture publiée de {@code facturation} (spec 08 §3.11, RG-FAC-31), pour {@code statistiques} : les ventes
 * d'une période. Appelée par un contexte, sans acteur : le contexte appelant contrôle ses propres droits.
 */
public interface FacturationLecture {

    /**
     * RG-FAC-30, 31 : les lignes vendues des pièces émises du {@code du} au {@code au} inclus (date d'émission),
     * par date d'émission puis numéro ; filtrées, si elle est donnée, par nature d'origine ({@code DSM}). Un avoir
     * n'a pas d'origine : le filtre l'écarte.
     */
    List<LigneVendue> ventes(LocalDate du, LocalDate au, Optional<String> nature);
}
