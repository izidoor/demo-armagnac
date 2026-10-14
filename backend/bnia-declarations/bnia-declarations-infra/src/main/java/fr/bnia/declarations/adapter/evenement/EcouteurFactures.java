package fr.bnia.declarations.adapter.evenement;

import fr.bnia.declarations.application.EnregistrerFactures;
import fr.bnia.declarations.domain.partage.AvoirEmis;
import fr.bnia.declarations.domain.partage.FactureEmise;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Les factures et avoirs de {@code facturation} (A-DEC-4), reçus dans sa transaction (ADR-013) : la projection
 * locale des factures des DSM (RG-DEC-34).
 */
@Component
class EcouteurFactures {

    private final EnregistrerFactures factures;

    EcouteurFactures(EnregistrerFactures factures) {
        this.factures = factures;
    }

    @EventListener
    void factureEmise(FactureEmise evenement) {
        factures.factureEmise(evenement);
    }

    @EventListener
    void avoirEmis(AvoirEmis evenement) {
        factures.avoirEmis(evenement);
    }
}
