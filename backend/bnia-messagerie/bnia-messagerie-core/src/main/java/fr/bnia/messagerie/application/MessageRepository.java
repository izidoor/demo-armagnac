package fr.bnia.messagerie.application;

import fr.bnia.messagerie.domain.Cote;
import fr.bnia.messagerie.domain.Message;
import java.util.List;
import java.util.Optional;

/** Port d'écriture des messages (spec 15 §3.4), à part de leur conversation. */
public interface MessageRepository {

    /** @return le message, avec son identifiant */
    Message inserer(Message message);

    /** Texte, date de modification et états de lecture. */
    void mettreAJour(Message message);

    Optional<Message> parId(long id);

    /** Du plus ancien au plus récent. */
    List<Message> deLaConversation(long conversation);

    int compter(long conversation);

    void supprimer(long id);

    /** RG-MSG-10 : tous les messages de la conversation, lus pour ce côté. */
    void marquerLus(long conversation, Cote cote);
}
