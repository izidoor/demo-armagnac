package fr.bnia.declarations.adapter.evenement;

import fr.bnia.declarations.application.EnregistrerFactures;
import fr.bnia.declarations.application.EnregistrerFactures.PieceRecue;
import fr.bnia.facturation.api.FactureAbandonnee;
import fr.bnia.facturation.api.FactureEmise;
import fr.bnia.operateurs.domain.partage.Acteur;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Les factures émises par {@code facturation} (ADR-021), reçues dans sa transaction (ADR-013, RG-FAC-25) : celles
 * des DSM, reconnues par leur origine, alimentent la projection locale (RG-DEC-34), comme leurs brouillons
 * abandonnés. Les avoirs suivront au lot 4 de la spec 08.
 */
@Component
class EcouteurFactures {

    /** L'origine des factures de DSM, telle que {@code FacturationAdapter} la demande. */
    static final String SOURCE = "declarations";
    static final String NATURE = "DSM";

    private final EnregistrerFactures factures;

    EcouteurFactures(EnregistrerFactures factures) {
        this.factures = factures;
    }

    @EventListener
    void factureEmise(FactureEmise evenement) {
        if (SOURCE.equals(evenement.origine().source()) && NATURE.equals(evenement.origine().nature())) {
            factures.factureEmise(new PieceRecue(Long.parseLong(evenement.origine().identifiant()), evenement.piece(),
                    evenement.numero(), evenement.date(), evenement.ht(), evenement.tva(), evenement.ttc(),
                    new Acteur(evenement.emisePar().utilisateur())));
        }
    }

    /** RG-DEC-34 : le brouillon d'une DSM abandonné ; la DSM redevient à facturer (RG-FAC-18). */
    @EventListener
    void factureAbandonnee(FactureAbandonnee evenement) {
        if (SOURCE.equals(evenement.origine().source()) && NATURE.equals(evenement.origine().nature())) {
            factures.factureAbandonnee(Long.parseLong(evenement.origine().identifiant()), evenement.piece(),
                    new Acteur(evenement.abandonneePar().utilisateur()));
        }
    }
}
