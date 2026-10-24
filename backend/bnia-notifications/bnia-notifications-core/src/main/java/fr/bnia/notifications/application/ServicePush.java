package fr.bnia.notifications.application;

import fr.bnia.notifications.domain.Abonnement;
import java.util.List;
import java.util.Optional;

/** Port vers les services push des navigateurs (Web Push, RFC 8030, 8291, 8292 ; ADR-024). */
public interface ServicePush {

    /**
     * RG-NOT-07 : la clé publique VAPID du serveur, en base64url, que le navigateur demande pour s'abonner ; vide si
     * l'installation n'a pas de clés, et alors pas de notification push.
     */
    Optional<String> clePublique();

    /**
     * RG-NOT-03, 04 : envoie le message à chaque appareil, après la validation de la transaction en cours ; un
     * abonnement expiré est retiré, tout autre échec est journalisé et n'annule rien.
     */
    void envoyer(List<Abonnement> appareils, MessagePush message);
}
