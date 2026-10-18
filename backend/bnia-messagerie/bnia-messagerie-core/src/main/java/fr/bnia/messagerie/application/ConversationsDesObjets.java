package fr.bnia.messagerie.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.messagerie.api.Acteur;
import fr.bnia.messagerie.api.Conversations;
import fr.bnia.messagerie.api.DescriptionObjet;
import fr.bnia.messagerie.api.ObjetRef;
import fr.bnia.messagerie.api.ObjetsConversables;
import fr.bnia.messagerie.domain.Auteur;
import fr.bnia.messagerie.domain.Conversation;
import fr.bnia.messagerie.domain.Message;
import fr.bnia.messagerie.domain.ObjetMetier;
import java.time.Clock;
import java.util.List;

/**
 * {@link Conversations}, appelé par les contextes métier dans leur transaction (A-MSG-4) : le motif d'une demande de
 * compléments, la suppression avec l'objet. Sans contrôle de droits : le contexte métier l'a fait. RG-MSG-04, 07, 08.
 */
@UseCase
public class ConversationsDesObjets implements Conversations {

    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final DroitsMessagerie droits;
    private final Objets objets;
    private final Clock horloge;

    public ConversationsDesObjets(ConversationRepository conversations, MessageRepository messages,
            DroitsMessagerie droits, List<ObjetsConversables> conversables, Clock horloge) {
        this.conversations = conversations;
        this.messages = messages;
        this.droits = droits;
        this.objets = new Objets(conversables);
        this.horloge = horloge;
    }

    @Override
    public void deposerMotif(ObjetRef objet, Acteur auteur, String texte) {
        Conversation conversation = conversations.parObjet(objet).orElseGet(() -> {
            DescriptionObjet description = objets.decrire(objet);
            return conversations.inserer(Conversation.surObjet(new ObjetMetier(objet, description.libelle()),
                    description.operateur(), horloge.instant()));
        });
        Auteur nomme = new Auteur(auteur.utilisateur(), droits.nom(auteur.utilisateur())
                .orElse("Utilisateur n° " + auteur.utilisateur()));
        messages.inserer(Message.motif(conversation.id(), nomme, texte, horloge.instant()));
    }

    /** L'objet a une conversation ; sans contrôle de droits ni lecture (chargeurs de démonstration). */
    public boolean ouverte(ObjetRef objet) {
        return conversations.parObjet(objet).isPresent();
    }

    @Override
    public void supprimer(ObjetRef objet) {
        conversations.parObjet(objet).ifPresent(c -> conversations.supprimer(c.id()));
    }
}
