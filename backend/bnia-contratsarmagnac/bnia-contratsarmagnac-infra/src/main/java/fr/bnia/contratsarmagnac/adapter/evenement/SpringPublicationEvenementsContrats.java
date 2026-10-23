package fr.bnia.contratsarmagnac.adapter.evenement;

import fr.bnia.contratsarmagnac.application.PublicationEvenements;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/** Événements publiés en {@code ApplicationEvent} Spring, synchrones (ADR-013). */
@Component
class SpringPublicationEvenementsContrats implements PublicationEvenements {

    private final ApplicationEventPublisher publisher;

    SpringPublicationEvenementsContrats(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publier(Object evenement) {
        publisher.publishEvent(evenement);
    }
}
