package fr.bnia.referentiels.application;

import java.util.List;

/** Listing des contenances au back-office, en SQL direct (ADR-004). */
public interface ContenancesLecture {

    /** Contenances triées par volume croissant ; la recherche porte sur le libellé saisi et le volume ({@code 0.7}). */
    List<ContenanceLigne> lister(FiltreValeurs filtre);
}
