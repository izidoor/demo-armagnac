package fr.bnia.notifications.application;

import fr.bnia.notifications.domain.Abonnement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Service push des tests : retient chaque envoi ; sans clé, l'installation n'envoie rien. */
final class PushCaptures implements ServicePush {

    record Envoi(List<Abonnement> appareils, MessagePush message) {
    }

    final List<Envoi> envois = new ArrayList<>();
    Optional<String> cle = Optional.of("BP4z9KsN6nGRTbVYI_c7VJSPQTBtkgcy27mlmlMoZIIgDll6e3vCYLocInmYWAmS6TlzAC8wEqKK6PBru3jl7A8");

    @Override
    public Optional<String> clePublique() {
        return cle;
    }

    @Override
    public void envoyer(List<Abonnement> appareils, MessagePush message) {
        envois.add(new Envoi(appareils, message));
    }
}
