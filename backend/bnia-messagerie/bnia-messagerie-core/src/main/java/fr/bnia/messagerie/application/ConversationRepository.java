package fr.bnia.messagerie.application;

import fr.bnia.messagerie.api.ObjetRef;
import fr.bnia.messagerie.domain.Conversation;
import java.util.Optional;

/** Port d'écriture des conversations (spec 15 §3.1). */
public interface ConversationRepository {

    /** @return la conversation, avec son identifiant */
    Conversation inserer(Conversation conversation);

    Optional<Conversation> parId(long id);

    /** RG-MSG-04 : la conversation d'un objet, au plus une. */
    Optional<Conversation> parObjet(ObjetRef objet);

    /** Avec ses messages. */
    void supprimer(long id);
}
