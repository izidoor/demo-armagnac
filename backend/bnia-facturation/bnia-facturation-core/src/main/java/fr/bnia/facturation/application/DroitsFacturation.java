package fr.bnia.facturation.application;

import fr.bnia.facturation.api.Acteur;
import java.util.Optional;

/**
 * Port de l'hôte (A-FAC-3) : les droits d'un acteur sur la facturation, évalués à chaque appel (RG-FAC-26),
 * implémenté dans l'infra sur la partie publiée d'{@code operateurs}.
 */
public interface DroitsFacturation {

    /** Gère la facturation : rôle {@code AGENT} ou {@code ADMIN} tenu dans l'opérateur BNIA. */
    boolean gere(Acteur acteur);

    /** Lit toutes les pièces : personnel du BNIA. */
    boolean litTout(Acteur acteur);

    /** Copie du nom de l'utilisateur, pour le journal (ADR-011 amendé) ; vide s'il n'existe plus. */
    Optional<String> nom(long utilisateur);
}
