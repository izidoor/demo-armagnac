package fr.bnia.declarations.application;

import fr.bnia.declarations.application.ConsulterStocksDeclares.LigneStockListee;
import java.time.LocalDate;
import java.util.List;

/** Production de l'état des stocks déclarés (ADR-020, adapter {@code document}). */
public interface EditionsStocks {

    /** UC-DEC-1 : le classeur Excel des stocks déclarés, pour les douanes, établi ce jour. */
    byte[] excel(List<LigneStockListee> lignes, LocalDate etabliLe);
}
