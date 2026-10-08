package fr.bnia.commun.adapter.rest;

import java.util.Optional;

/**
 * L'utilisateur de la session en cours, tel que l'authentification l'a établi. Implémenté par
 * {@code authentification} (ADR-015) : les autres contextes ne voient que {@link Connecte}.
 */
public interface SessionUtilisateur {

    /** Vide si la requête n'est pas authentifiée (ou pas encore : code de vérification attendu). */
    Optional<Long> utilisateurConnecte();
}
