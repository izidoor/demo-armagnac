package fr.bnia.facturation.application;

import fr.bnia.facturation.domain.EtatPiece;
import fr.bnia.facturation.domain.SensPiece;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Filtres de l'historique du back-office (RG-FAC-27) ; chacun est facultatif.
 *
 * @param numero  début du numéro
 * @param nature  nature d'origine ({@code DSM})
 * @param client  identifiant du client (fiche opérateur)
 * @param acheteur texte contenu dans le nom de l'acheteur ou le code du client
 * @param periode texte contenu dans le libellé de la période
 * @param du      date d'émission au plus tôt
 * @param au      date d'émission au plus tard
 */
public record FiltrePieces(Optional<String> numero, Optional<SensPiece> sens, Optional<EtatPiece> etat,
        Optional<String> nature, Optional<String> client, Optional<String> acheteur, Optional<String> periode,
        Optional<LocalDate> du, Optional<LocalDate> au) {

    public static FiltrePieces aucun() {
        return new FiltrePieces(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }
}
