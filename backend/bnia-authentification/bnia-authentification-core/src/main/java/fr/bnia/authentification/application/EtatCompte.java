package fr.bnia.authentification.application;

import java.time.Instant;
import java.util.Optional;

/**
 * Ce que l'écran « Utilisateurs » de {@code operateurs} montre d'un compte (RG-OPE-53, spec 03 §5.2) :
 * blocage temporaire en cours, dernière connexion, premier mot de passe posé ou non.
 */
public record EtatCompte(long utilisateur, Optional<Instant> bloqueJusqua, Optional<Instant> derniereConnexion,
        boolean motDePasseDefini) {
}
