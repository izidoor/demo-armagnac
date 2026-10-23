package fr.bnia.declarations.adapter.evenement;

import fr.bnia.declarations.application.InscrireRetiraisons;
import fr.bnia.declarations.domain.partage.RetiraisonValidee;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Les retiraisons validées des contrats d'achat Armagnac (spec 06 A-CA-3), reçues dans la transaction de
 * {@code contratsarmagnac} (ADR-013) : un échec de l'inscription annule la validation.
 */
@Component
class EcouteurRetiraisons {

    private final InscrireRetiraisons inscrire;

    EcouteurRetiraisons(InscrireRetiraisons inscrire) {
        this.inscrire = inscrire;
    }

    @EventListener
    void retiraisonValidee(RetiraisonValidee evenement) {
        inscrire.retiraisonValidee(evenement);
    }
}
