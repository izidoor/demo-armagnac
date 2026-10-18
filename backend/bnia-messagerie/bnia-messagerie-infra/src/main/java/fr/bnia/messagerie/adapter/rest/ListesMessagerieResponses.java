package fr.bnia.messagerie.adapter.rest;

import fr.bnia.messagerie.api.TypeObjet;
import fr.bnia.messagerie.application.ConsulterMessagerie.Liste;
import fr.bnia.messagerie.application.DroitsMessagerie.OperateurNomme;
import fr.bnia.messagerie.application.MessagerieLecture.ConversationListee;
import fr.bnia.messagerie.application.MessagerieLecture.NouveauMessage;
import fr.bnia.messagerie.adapter.rest.ConversationResponse.ObjetResponse;
import fr.bnia.messagerie.adapter.rest.ConversationResponse.OperateurResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Les listes de la messagerie (spec 15 §6) : nouveaux messages et conversations. */
final class ListesMessagerieResponses {

    private ListesMessagerieResponses() {
    }

    @Schema(name = "NouveauxMessages", description = "Les messages non lus de mon côté (RG-MSG-11)")
    record NouveauxMessagesResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Le nombre du tableau de bord") int nombre,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Du plus récent au plus ancien")
            List<NouveauMessageResponse> messages) {

        static NouveauxMessagesResponse depuis(Liste<NouveauMessage> liste) {
            return new NouveauxMessagesResponse(liste.elements().size(), liste.elements().stream()
                    .map(m -> NouveauMessageResponse.depuis(m, liste.operateurs())).toList());
        }
    }

    @Schema(name = "NouveauMessage", description = "Un message non lu, en vue tronquée (RG-MSG-11)")
    record NouveauMessageResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long conversation,
            @Schema(description = "L'objet de la conversation ; vide pour un sujet libre") ObjetResponse objet,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Libellé de l'objet ou titre du sujet libre")
            String intitule,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) OperateurResponse operateur,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String auteur,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant ecritLe,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Première ligne, tronquée") String debut) {

        static NouveauMessageResponse depuis(NouveauMessage m, Map<Long, OperateurNomme> operateurs) {
            return new NouveauMessageResponse(m.message(), m.conversation(), objetDe(m.type(), m.identifiant(), m.intitule()),
                    m.intitule(), operateurDe(m.operateur(), operateurs), m.auteur(), m.ecritLe(),
                    Extraits.premiereLigne(m.texte()));
        }
    }

    @Schema(name = "ConversationListee", description = "Une conversation de « Mes conversations » (RG-MSG-15, 16)")
    record ConversationListeeResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
            @Schema(description = "L'objet ; vide pour un sujet libre") ObjetResponse objet,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Libellé de l'objet ou titre du sujet libre")
            String intitule,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) OperateurResponse operateur,
            @Schema(description = "Auteur du sujet libre") String auteur,
            @Schema(description = "Date du dernier message") Instant dernierMessage,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Non lus de mon côté") int nonLus,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int messages,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                    description = "Le message le plus récent qui correspond à la recherche, ou le dernier ; tronqué")
            String extrait) {

        static List<ConversationListeeResponse> depuis(Liste<ConversationListee> liste) {
            return liste.elements().stream().map(c -> new ConversationListeeResponse(c.id(),
                    objetDe(c.type(), c.identifiant(), c.intitule()), c.intitule(),
                    operateurDe(c.operateur(), liste.operateurs()), c.auteurLibre().orElse(null), c.dernierMessage(),
                    c.nonLus(), c.messages(), Extraits.premiereLigne(c.extrait()))).toList();
        }
    }

    private static ObjetResponse objetDe(Optional<TypeObjet> type, Optional<String> identifiant, String libelle) {
        return type.map(t -> new ObjetResponse(t, identifiant.orElseThrow(), libelle)).orElse(null);
    }

    private static OperateurResponse operateurDe(long id, Map<Long, OperateurNomme> operateurs) {
        return OperateurResponse.depuis(operateurs.getOrDefault(id, new OperateurNomme(id, "", "Opérateur n° " + id)));
    }
}
