package fr.bnia.authentification.application;

import fr.bnia.authentification.domain.EvenementConnexion;
import java.time.Instant;

/**
 * Entrée du journal des connexions (spec 03 §3.6, RG-AUT-12) : jamais de mot de passe ni de code.
 *
 * @param identifiantSaisi tel que saisi, même s'il ne correspond à aucun compte
 * @param utilisateur      sans clé étrangère, vide si l'identifiant est inconnu ; survit à la suppression du compte
 * @param detail           motif d'un refus, ou « navigateur de confiance »
 */
public record LigneConnexion(Instant date, String identifiantSaisi, Long utilisateur, EvenementConnexion evenement,
        String detail, String adresseIp, String navigateur) {
}
