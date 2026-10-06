package fr.bnia.referentiels.application;

import java.time.LocalDate;
import java.util.List;

/** Listing des classes de TVA au back-office, en SQL direct (ADR-004). */
public interface ClassesTvaLecture {

    /** Classes triées par libellé, avec le taux en vigueur à la date donnée. */
    List<ClasseTvaLigne> lister(FiltreValeurs filtre, LocalDate date);
}
