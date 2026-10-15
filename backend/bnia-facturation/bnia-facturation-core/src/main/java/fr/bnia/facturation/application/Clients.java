package fr.bnia.facturation.application;

import fr.bnia.facturation.api.ClientRef;
import fr.bnia.facturation.domain.Acheteur;
import java.util.Optional;

/**
 * Port de l'hôte (A-FAC-3, spec 08 §3.11) : l'acheteur d'un client, implémenté dans l'infra sur la partie
 * publiée d'{@code operateurs}. Lu à la demande et de nouveau à l'émission (RG-FAC-11).
 */
public interface Clients {

    /** L'acheteur de ce client, tel qu'il est aujourd'hui ; vide si le client n'existe pas. */
    Optional<Acheteur> acheteur(ClientRef client);
}
