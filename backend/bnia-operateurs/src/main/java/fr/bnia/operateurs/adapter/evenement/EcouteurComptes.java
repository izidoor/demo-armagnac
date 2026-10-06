package fr.bnia.operateurs.adapter.evenement;

import fr.bnia.operateurs.application.GererContacts;
import fr.bnia.operateurs.domain.partage.CompteCree;
import fr.bnia.operateurs.domain.partage.IdentifiantCompteModifie;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Événements de {@code acces} sur les comptes portail (RG-OPE-32, ADR-013), traités dans la transaction
 * de l'émetteur. Tant que {@code acces} n'existe pas, personne ne les émet.
 */
@Component
class EcouteurComptes {

    private final GererContacts contacts;

    EcouteurComptes(GererContacts contacts) {
        this.contacts = contacts;
    }

    @EventListener
    void compteCree(CompteCree evenement) {
        contacts.verrouillerEmail(evenement.email());
    }

    @EventListener
    void identifiantModifie(IdentifiantCompteModifie evenement) {
        contacts.reporterIdentifiant(evenement.ancien(), evenement.nouveau());
    }
}
