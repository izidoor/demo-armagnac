package fr.bnia.facturation.adapter.evenement;

import fr.bnia.facturation.application.PublicationEvenements;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/** Événements publiés en {@code ApplicationEvent} Spring, synchrones, dans la transaction (ADR-013, RG-FAC-25). */
@Component
class SpringPublicationEvenementsFacturation implements PublicationEvenements {

    private final ApplicationEventPublisher publisher;

    SpringPublicationEvenementsFacturation(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publier(Object evenement) {
        publisher.publishEvent(evenement);
    }
}
