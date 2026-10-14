package fr.bnia.declarations.application;

import java.time.Instant;
import java.time.LocalDate;

/** Retards déjà signalés (RG-DEC-41, A-DEC-7, ADR-019) : l'idempotence de la détection, en base. */
public interface RetardsSignales {

    /**
     * Note le retard de la période de l'opérateur qui commence à {@code debut}, s'il ne l'est pas déjà.
     *
     * @return vrai s'il vient d'être noté : l'événement est à publier ; faux s'il l'était déjà
     */
    boolean signaler(long operateur, LocalDate debut, Instant le);
}
