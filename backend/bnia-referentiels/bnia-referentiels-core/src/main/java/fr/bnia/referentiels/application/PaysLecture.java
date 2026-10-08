package fr.bnia.referentiels.application;

import java.util.List;

/** Listing des pays au back-office, en SQL direct (ADR-004). */
public interface PaysLecture {

    /** Pays triés par libellé ; la recherche porte sur le code, le code ISO et le libellé. */
    List<PaysLigne> lister(FiltreValeurs filtre);
}
