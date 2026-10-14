package fr.bnia.declarations.adapter.evenement;

import fr.bnia.declarations.application.PublicationEvenements;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/** Événements publiés en {@code ApplicationEvent} Spring, synchrones (ADR-013). */
@Component
class SpringPublicationEvenementsDeclarations implements PublicationEvenements {

    private final ApplicationEventPublisher publisher;

    SpringPublicationEvenementsDeclarations(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publier(Object evenement) {
        publisher.publishEvent(evenement);
    }
}
