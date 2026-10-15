package fr.bnia.facturation.application;

import fr.bnia.facturation.api.ClientRef;
import fr.bnia.facturation.api.Origine;
import fr.bnia.facturation.domain.EtatPiece;
import fr.bnia.facturation.domain.SensPiece;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Une pièce de l'historique (RG-FAC-27).
 *
 * @param numero       {@code null} pour un brouillon
 * @param dateEmission {@code null} pour un brouillon
 * @param client       {@code null} pour un client libre
 * @param origine      {@code null} pour un avoir
 */
public record PieceLigne(long id, SensPiece sens, EtatPiece etat, String numero, LocalDate dateEmission,
        String nomAcheteur, ClientRef client, Origine origine, String libellePeriode, BigDecimal ht, BigDecimal tva,
        BigDecimal ttc, Instant creeeLe) {
}
