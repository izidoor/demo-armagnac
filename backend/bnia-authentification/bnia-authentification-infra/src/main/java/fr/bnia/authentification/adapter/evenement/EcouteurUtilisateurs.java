package fr.bnia.authentification.adapter.evenement;

import fr.bnia.authentification.application.GererComptes;
import fr.bnia.operateurs.domain.partage.EmailUtilisateurModifie;
import fr.bnia.operateurs.domain.partage.UtilisateurCree;
import fr.bnia.operateurs.domain.partage.UtilisateurSupprime;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Événements de {@code operateurs} sur ses utilisateurs, traités dans la transaction de l'émetteur
 * (ADR-013, A-AUT-1) : un échec ici annule l'action de {@code operateurs}.
 */
@Component
class EcouteurUtilisateurs {

    private final GererComptes comptes;

    EcouteurUtilisateurs(GererComptes comptes) {
        this.comptes = comptes;
    }

    /** RG-AUT-11. */
    @EventListener
    void utilisateurCree(UtilisateurCree evenement) {
        comptes.creer(evenement.utilisateur(), evenement.email());
    }

    /** RG-AUT-10. */
    @EventListener
    void emailModifie(EmailUtilisateurModifie evenement) {
        comptes.changerIdentifiant(evenement.utilisateur(), evenement.email());
    }

    /** RG-AUT-11. */
    @EventListener
    void utilisateurSupprime(UtilisateurSupprime evenement) {
        comptes.supprimer(evenement.utilisateur());
    }
}
