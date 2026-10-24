package fr.bnia.support;

import fr.bnia.notifications.application.MessagePush;
import fr.bnia.notifications.application.ServicePush;
import fr.bnia.notifications.domain.Abonnement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Service push des tests d'intégration (spec 11) : à la place de Web Push, retient chaque message et l'appareil visé,
 * après la validation de la transaction comme le vrai.
 */
public final class PushCaptures implements ServicePush {

    /** Une clé publique P-256 valide (RFC 8291, annexe A). */
    public static final String CLE_PUBLIQUE =
            "BP4z9KsN6nGRTbVYI_c7VJSPQTBtkgcy27mlmlMoZIIgDll6e3vCYLocInmYWAmS6TlzAC8wEqKK6PBru3jl7A8";

    public record Recu(String adresse, MessagePush message) {
    }

    private final List<Recu> recus = new ArrayList<>();

    @Override
    public Optional<String> clePublique() {
        return Optional.of(CLE_PUBLIQUE);
    }

    @Override
    public void envoyer(List<Abonnement> appareils, MessagePush message) {
        Runnable capture = () -> {
            synchronized (recus) {
                appareils.forEach(a -> recus.add(new Recu(a.adresse(), message)));
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    capture.run();
                }
            });
        } else {
            capture.run();
        }
    }

    /** Les messages reçus par cet appareil, dans l'ordre. */
    public List<MessagePush> recusPar(String adresse) {
        synchronized (recus) {
            return recus.stream().filter(r -> r.adresse().equals(adresse)).map(Recu::message).toList();
        }
    }
}
