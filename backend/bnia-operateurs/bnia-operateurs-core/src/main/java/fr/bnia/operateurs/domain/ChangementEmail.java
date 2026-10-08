package fr.bnia.operateurs.domain;

import java.time.Instant;

/**
 * Demande de changement d'e-mail en cours d'un utilisateur (RG-OPE-48, vision UC-24) : une seule à la fois.
 *
 * @param empreinte empreinte du jeton envoyé au nouvel e-mail ({@link Jeton})
 */
public record ChangementEmail(Email nouvelEmail, String empreinte, Instant expireLe) {

    public boolean expiree(Instant maintenant) {
        return !maintenant.isBefore(expireLe);
    }
}
