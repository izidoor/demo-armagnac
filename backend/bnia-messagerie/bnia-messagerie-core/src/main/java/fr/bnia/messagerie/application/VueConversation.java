package fr.bnia.messagerie.application;

import fr.bnia.messagerie.domain.Conversation;
import fr.bnia.messagerie.domain.Cote;
import fr.bnia.messagerie.domain.Message;
import fr.bnia.messagerie.domain.Sujet;
import java.util.List;
import java.util.Optional;

/**
 * Une conversation telle que l'acteur la voit (spec 15 §6) : son sujet, son opérateur, ses messages du plus ancien
 * au plus récent, avec ceux qui étaient non lus de son côté avant l'affichage (RG-MSG-10), et ce qu'il peut y faire.
 *
 * @param conversation la conversation ; vide pour un objet qui n'en a pas encore (RG-MSG-04)
 * @param lectureSeule pourquoi l'acteur n'y écrit pas ; vide s'il écrit
 */
public record VueConversation(Optional<Conversation> conversation, Sujet sujet, long operateur,
        DroitsMessagerie.OperateurNomme nommeOperateur, Cote cote, List<MessageVu> messages, boolean peutEcrire,
        Optional<String> lectureSeule, boolean auteurParti) {

    /** Un message et son état avant l'affichage, pour le mettre en évidence. */
    public record MessageVu(Message message, boolean nonLu) {
    }
}
