package fr.bnia.authentification.domain;

import java.time.Instant;

/**
 * Un secret en cours (code de vérification, lien de réinitialisation), conservé en empreinte, valable
 * jusqu'à une date (RG-AUT-05, 08).
 */
public record Echeance(String empreinte, Instant expireLe) {

    /** Vrai si le secret saisi correspond et n'a pas expiré. */
    boolean accepte(String clair, Instant maintenant) {
        return clair != null && maintenant.isBefore(expireLe) && Secrets.egales(empreinte, Secrets.empreinte(clair));
    }
}
