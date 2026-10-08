package fr.bnia.referentiels.application;

import java.util.List;

/** Listing des régions au back-office, en SQL direct (ADR-004). */
public interface RegionsLecture {

    /** Régions triées par libellé. */
    List<RegionLigne> lister(FiltreValeurs filtre);
}
