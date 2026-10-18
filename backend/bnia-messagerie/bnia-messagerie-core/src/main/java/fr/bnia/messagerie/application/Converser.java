package fr.bnia.messagerie.application;

import fr.bnia.commun.application.FormatDocument;
import fr.bnia.commun.application.StockageDocuments;
import fr.bnia.commun.application.StockageDocuments.DocumentLu;
import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.DocumentDepose;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.messagerie.api.Acteur;
import fr.bnia.messagerie.api.DescriptionObjet;
import fr.bnia.messagerie.api.ObjetRef;
import fr.bnia.messagerie.api.ObjetsConversables;
import fr.bnia.messagerie.domain.Auteur;
import fr.bnia.messagerie.domain.Conversation;
import fr.bnia.messagerie.domain.Cote;
import fr.bnia.messagerie.domain.Message;
import fr.bnia.messagerie.domain.ObjetMetier;
import fr.bnia.messagerie.domain.SujetLibre;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Lire et écrire une conversation (UC-MSG-1 à 3, 7, 8, 11 ; spec 15 §5.1) : depuis la page d'un objet ou depuis
 * la messagerie ; ouvrir une conversation libre, avec pièces jointes. Afficher une conversation la lit pour le côté
 * de l'acteur (RG-MSG-10). RG-MSG-01 à 06, 09, 10, 12 à 14.
 */
@UseCase
public class Converser {

    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final DroitsMessagerie droits;
    private final Acces acces;
    /** RG-MSG-14 : les formats admis en pièce jointe. */
    private static final Set<FormatDocument> FORMATS = EnumSet.allOf(FormatDocument.class);

    private final Objets objets;
    private final StockageDocuments stockage;
    private final Clock horloge;

    public Converser(ConversationRepository conversations, MessageRepository messages, DroitsMessagerie droits,
            List<ObjetsConversables> conversables, StockageDocuments stockage, Clock horloge) {
        this.conversations = conversations;
        this.messages = messages;
        this.droits = droits;
        this.acces = new Acces(droits);
        this.objets = new Objets(conversables);
        this.stockage = stockage;
        this.horloge = horloge;
    }

    /** RG-MSG-01, 04, 10 : la conversation d'un objet, vide s'il n'en a pas encore ; affichée, elle est lue. */
    public VueConversation consulterObjet(Acteur acteur, ObjetRef objet) {
        Optional<Conversation> existante = conversations.parObjet(objet);
        if (existante.isPresent()) {
            return afficher(acteur, existante.get());
        }
        DescriptionObjet description = objets.decrire(objet);
        Acces.Droit droit = acces.surObjet(acteur, objet.type(), description.operateur(), description.libelle());
        return new VueConversation(Optional.empty(), new ObjetMetier(objet, description.libelle()),
                description.operateur(), nomme(description.operateur()), droit.cote(), List.of(), droit.peutEcrire(),
                droit.lectureSeule(), false);
    }

    /** RG-MSG-01, 02, 10 : une conversation de la messagerie ; affichée, elle est lue. */
    public VueConversation consulter(Acteur acteur, long id) {
        return afficher(acteur, conversation(id));
    }

    /** RG-MSG-01, 03 à 05, 09 : écrire sur un objet ; sa conversation naît au premier message. */
    public Message ecrireSurObjet(Acteur acteur, ObjetRef objet, String texte) {
        Conversation conversation = conversations.parObjet(objet).orElse(null);
        if (conversation == null) {
            DescriptionObjet description = objets.decrire(objet);
            acces.surObjet(acteur, objet.type(), description.operateur(), description.libelle()).exigerEcriture();
            conversation = conversations.inserer(Conversation.surObjet(new ObjetMetier(objet, description.libelle()),
                    description.operateur(), maintenant()));
        }
        return ecrire(acteur, conversation, texte, List.of());
    }

    /** RG-MSG-01 à 03, 05, 09, 13 : répondre dans une conversation existante. */
    public Message ecrire(Acteur acteur, long id, String texte) {
        return ecrire(acteur, conversation(id), texte, List.of());
    }

    /** RG-MSG-02, 05, 13, 14 : répondre dans une conversation libre, avec des pièces jointes. */
    public Message ecrire(Acteur acteur, long id, String texte, List<FichierJoint> pieces) {
        return ecrire(acteur, conversation(id), texte, pieces);
    }

    /**
     * RG-MSG-02, 05, 12, 14 (UC-MSG-1) : un utilisateur du portail ouvre une conversation libre avec le BNIA, dans son
     * opérateur actif, avec un titre, un premier message et ses pièces jointes ; lui seul la voit dans son opérateur.
     */
    public VueConversation ouvrirLibre(Acteur acteur, long operateur, String titre, String texte,
            List<FichierJoint> pieces) {
        if (droits.cote(acteur, operateur).filter(c -> c == Cote.OPERATEUR).isEmpty()
                || !droits.gereMessagerie(acteur, operateur)) {
            throw new AccesRefuse("RG-MSG-12", "Seul un membre de l'opérateur ouvre une conversation avec le BNIA.");
        }
        SujetLibre sujet = new SujetLibre(titre, auteur(acteur));
        Message.ecrire(0, sujet.auteur(), Cote.OPERATEUR, texte, List.of(), maintenant());
        Conversation conversation = conversations.inserer(Conversation.libre(sujet, operateur, maintenant()));
        ecrire(acteur, conversation, texte, pieces);
        return afficher(acteur, conversation);
    }

    /** RG-MSG-14 : une pièce jointe d'une conversation que l'acteur peut lire. */
    public DocumentLu piece(Acteur acteur, long id, long cle) {
        Conversation conversation = conversation(id);
        acces.sur(acteur, conversation);
        boolean jointe = messages.deLaConversation(id).stream().flatMap(m -> m.pieces().stream())
                .anyMatch(d -> d.cle() == cle);
        if (!jointe) {
            throw new ElementIntrouvable("Pièce jointe introuvable : n° " + cle + ".");
        }
        return stockage.lire(cle);
    }

    /** RG-MSG-06 : l'auteur modifie son message ; son état de lecture ne change pas. */
    public Message modifier(Acteur acteur, long id, String texte) {
        Message message = message(id);
        acces.sur(acteur, conversation(message.conversation()));
        message.modifier(acteur.utilisateur(), texte, maintenant());
        messages.mettreAJour(message);
        return message;
    }

    /** RG-MSG-06 : l'auteur supprime son message, sans trace ; sa conversation, s'il était le dernier. */
    public void supprimer(Acteur acteur, long id) {
        Message message = message(id);
        acces.sur(acteur, conversation(message.conversation()));
        message.exigerSupprimable(acteur.utilisateur());
        messages.supprimer(id);
        stockage.supprimer(message.pieces().stream().map(DocumentDepose::cle).toList());
        if (messages.compter(message.conversation()) == 0) {
            conversations.supprimer(message.conversation());
        }
    }

    /** RG-MSG-10 : remet un message non lu pour le côté de l'acteur, jusqu'au prochain affichage. */
    public void marquerNonLu(Acteur acteur, long id) {
        Message message = message(id);
        Acces.Droit droit = acces.sur(acteur, conversation(message.conversation()));
        message.marquerNonLu(droit.cote());
        messages.mettreAJour(message);
    }

    private Message ecrire(Acteur acteur, Conversation conversation, String texte, List<FichierJoint> fichiers) {
        Acces.Droit droit = acces.sur(acteur, conversation);
        droit.exigerEcriture();
        if (!fichiers.isEmpty() && !conversation.libre()) {
            throw new RegleEnfreinte("RG-MSG-14", "Une conversation d'objet n'a pas de pièces jointes ; ouvrez une"
                    + " conversation avec le BNIA pour lui envoyer un fichier.");
        }
        Auteur auteur = auteur(acteur);
        // Valide le message avant de déposer ses pièces (RG-MSG-05, 14).
        Message.ecrire(conversation.id(), auteur, droit.cote(), texte, fichiers.stream()
                .map(f -> new DocumentDepose(0, f.nom(), "", 0)).toList(), maintenant());
        List<DocumentDepose> pieces = new ArrayList<>();
        for (FichierJoint fichier : fichiers) {
            pieces.add(stockage.deposer(fichier.nom(), fichier.contenu(), FORMATS));
        }
        return messages.inserer(Message.ecrire(conversation.id(), auteur, droit.cote(), texte, pieces, maintenant()));
    }

    private VueConversation afficher(Acteur acteur, Conversation conversation) {
        Acces.Droit droit = acces.sur(acteur, conversation);
        List<VueConversation.MessageVu> fil = messages.deLaConversation(conversation.id()).stream()
                .map(m -> new VueConversation.MessageVu(m, !m.luPar(droit.cote()))).toList();
        if (fil.stream().anyMatch(VueConversation.MessageVu::nonLu)) {
            messages.marquerLus(conversation.id(), droit.cote());
        }
        return new VueConversation(Optional.of(conversation), conversation.sujet(), conversation.operateur(),
                nomme(conversation.operateur()), droit.cote(), fil, droit.peutEcrire(), droit.lectureSeule(),
                droit.auteurParti());
    }

    private DroitsMessagerie.OperateurNomme nomme(long operateur) {
        return droits.operateurs(List.of(operateur)).getOrDefault(operateur,
                new DroitsMessagerie.OperateurNomme(operateur, "", "Opérateur n° " + operateur));
    }

    private Auteur auteur(Acteur acteur) {
        return new Auteur(acteur.utilisateur(), droits.nom(acteur.utilisateur())
                .orElse("Utilisateur n° " + acteur.utilisateur()));
    }

    private Conversation conversation(long id) {
        return conversations.parId(id)
                .orElseThrow(() -> new ElementIntrouvable("Conversation introuvable : n° " + id + "."));
    }

    private Message message(long id) {
        return messages.parId(id).orElseThrow(() -> new ElementIntrouvable("Message introuvable : n° " + id + "."));
    }

    private Instant maintenant() {
        return horloge.instant();
    }
}
