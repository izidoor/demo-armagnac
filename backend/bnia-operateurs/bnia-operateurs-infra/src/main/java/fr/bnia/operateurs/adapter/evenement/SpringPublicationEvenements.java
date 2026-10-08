package fr.bnia.operateurs.adapter.evenement;

import fr.bnia.operateurs.application.PublicationEvenements;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/** Événements publiés en {@code ApplicationEvent} Spring, synchrones (ADR-013). */
@Component
class SpringPublicationEvenements implements PublicationEvenements {

    private final ApplicationEventPublisher publisher;

    SpringPublicationEvenements(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publier(Object evenement) {
        publisher.publishEvent(evenement);
    }
}
