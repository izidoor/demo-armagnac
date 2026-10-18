package fr.bnia.messagerie.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.messagerie.api.Acteur;
import fr.bnia.messagerie.application.DroitsMessagerie.OperateurNomme;
import fr.bnia.messagerie.application.MessagerieLecture.ConversationListee;
import fr.bnia.messagerie.application.MessagerieLecture.Filtre;
import fr.bnia.messagerie.application.MessagerieLecture.NouveauMessage;
import fr.bnia.messagerie.application.MessagerieLecture.Portee;
import fr.bnia.messagerie.domain.Cote;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * La messagerie d'un acteur (UC-MSG-6, 9, 10 ; spec 15 §5.2) : ses nouveaux messages, que compte son tableau de bord,
 * et ses conversations, filtrées et cherchées. Au back-office, celles de tous les opérateurs ; au portail, celles de
 * l'opérateur actif. Lire la liste ne lit aucun message. RG-MSG-11, 15, 16.
 */
@UseCase
public class ConsulterMessagerie {

    private final MessagerieLecture lecture;
    private final DroitsMessagerie droits;

    public ConsulterMessagerie(MessagerieLecture lecture, DroitsMessagerie droits) {
        this.lecture = lecture;
        this.droits = droits;
    }

    /** Une liste, avec les opérateurs qu'elle cite. */
    public record Liste<T>(List<T> elements, Map<Long, OperateurNomme> operateurs) {
    }

    /** RG-MSG-11 : les nouveaux messages du BNIA, de tous les opérateurs. */
    public Liste<NouveauMessage> nouveauxDuBnia(Acteur acteur) {
        return nomme(lecture.nouveaux(porteeBnia(acteur)), NouveauMessage::operateur);
    }

    /** RG-MSG-11 : les nouveaux messages de l'opérateur actif, pour l'acteur qui en est membre. */
    public Liste<NouveauMessage> nouveauxDuPortail(Acteur acteur, long operateur) {
        return nomme(lecture.nouveaux(porteePortail(acteur, operateur)), NouveauMessage::operateur);
    }

    /** RG-MSG-15, 16 : les conversations du BNIA, filtrées par opérateur, type ou sujet libre, et cherchées. */
    public Liste<ConversationListee> conversationsDuBnia(Acteur acteur, Filtre filtre) {
        return nomme(lecture.conversations(porteeBnia(acteur), nettoye(filtre)), ConversationListee::operateur);
    }

    /** RG-MSG-15, 16 : les conversations de l'opérateur actif que l'acteur peut lire. */
    public Liste<ConversationListee> conversationsDuPortail(Acteur acteur, long operateur, Filtre filtre) {
        Filtre surLOperateur = new Filtre(Optional.empty(), filtre.type(), filtre.libres(), filtre.nonLuesSeulement(),
                filtre.recherche());
        return nomme(lecture.conversations(porteePortail(acteur, operateur), nettoye(surLOperateur)),
                ConversationListee::operateur);
    }

    private Portee porteeBnia(Acteur acteur) {
        if (!droits.personnelBnia(acteur)) {
            throw new AccesRefuse("RG-MSG-11", "La messagerie de tous les opérateurs est réservée au personnel du BNIA.");
        }
        return new Portee(Cote.BNIA, Optional.empty(), Optional.empty(), droits.typesLusParLeBnia(acteur));
    }

    private Portee porteePortail(Acteur acteur, long operateur) {
        if (droits.cote(acteur, operateur).filter(c -> c == Cote.OPERATEUR).isEmpty()) {
            throw new AccesRefuse("RG-MSG-11", "Vous n'êtes pas membre de cet opérateur.");
        }
        return new Portee(Cote.OPERATEUR, Optional.of(operateur), Optional.of(acteur.utilisateur()),
                droits.typesLus(acteur, operateur));
    }

    private static Filtre nettoye(Filtre filtre) {
        return new Filtre(filtre.operateur(), filtre.type(), filtre.libres(), filtre.nonLuesSeulement(),
                filtre.recherche().map(String::strip).filter(r -> !r.isEmpty()));
    }

    private <T> Liste<T> nomme(List<T> elements, Function<T, Long> operateur) {
        return new Liste<>(elements, droits.operateurs(elements.stream().map(operateur).distinct().toList()));
    }
}
