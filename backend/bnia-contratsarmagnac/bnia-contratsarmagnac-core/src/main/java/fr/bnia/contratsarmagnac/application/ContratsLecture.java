package fr.bnia.contratsarmagnac.application;

import fr.bnia.contratsarmagnac.domain.Mercuriale.LigneConclue;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Port de lecture des contrats : listes du portail et du back-office, onglet de la fiche opérateur (RG-CA-25). */
public interface ContratsLecture {

    /**
     * Les contrats, du plus récemment modifié au plus ancien.
     *
     * @param operateur partie au contrat, acheteur ou vendeur
     * @param numero    contenu dans le numéro, sans tenir compte de la casse
     */
    List<ContratLigne> lister(Optional<Long> operateur, Optional<String> numero);

    /** RG-CA-26 : les lignes, sur la version courante, des contrats conclus ou exécutés datés dans la période. */
    List<LigneConclue> lignesConclues(LocalDate du, LocalDate au);
}
