package fr.bnia.declarations.adapter.integration;

import fr.bnia.declarations.application.Messagerie;
import fr.bnia.messagerie.api.Conversations;
import fr.bnia.messagerie.api.ObjetRef;
import fr.bnia.messagerie.api.TypeObjet;
import fr.bnia.operateurs.domain.partage.Acteur;
import org.springframework.stereotype.Component;

/**
 * Le port {@link Messagerie} du core sur l'API de {@code messagerie} (ADR-023, A-MSG-1) : la conversation d'une DSM
 * est celle de l'objet {@code DSM} de même identifiant.
 */
@Component
class MessagerieAdapter implements Messagerie {

    private final Conversations conversations;

    MessagerieAdapter(Conversations conversations) {
        this.conversations = conversations;
    }

    @Override
    public void deposerMotif(long dsm, Acteur auteur, String motif) {
        conversations.deposerMotif(ObjetRef.de(TypeObjet.DSM, dsm), new fr.bnia.messagerie.api.Acteur(auteur.utilisateur()),
                motif);
    }

    @Override
    public void supprimerConversation(long dsm) {
        conversations.supprimer(ObjetRef.de(TypeObjet.DSM, dsm));
    }
}
