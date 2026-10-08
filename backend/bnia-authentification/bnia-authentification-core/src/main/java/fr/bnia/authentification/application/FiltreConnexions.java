package fr.bnia.authentification.application;

import fr.bnia.authentification.domain.EvenementConnexion;
import java.time.Instant;

/**
 * Filtres du journal des connexions (RG-AUT-13). Un filtre {@code null} ne filtre pas.
 *
 * @param du inclus
 * @param au exclu
 */
public record FiltreConnexions(Long utilisateur, EvenementConnexion evenement, Instant du, Instant au) {
}
