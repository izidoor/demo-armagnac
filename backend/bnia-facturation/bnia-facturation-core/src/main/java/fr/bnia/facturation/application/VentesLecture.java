package fr.bnia.facturation.application;

import fr.bnia.facturation.api.LigneVendue;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Port de lecture des ventes (RG-FAC-30, 31) : les lignes article des pièces émises d'une période. */
public interface VentesLecture {

    /**
     * Les lignes vendues émises du {@code du} au {@code au} inclus, par date d'émission, numéro et position ; celles
     * d'un avoir en négatif ; filtrées par nature d'origine et par client (identifiant chez l'hôte) s'ils sont donnés.
     */
    List<LigneVendue> ventes(LocalDate du, LocalDate au, Optional<String> nature, Optional<String> client);
}
