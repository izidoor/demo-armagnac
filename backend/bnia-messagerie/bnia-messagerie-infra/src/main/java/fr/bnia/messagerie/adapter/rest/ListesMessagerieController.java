package fr.bnia.messagerie.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.messagerie.adapter.rest.ListesMessagerieResponses.ConversationListeeResponse;
import fr.bnia.messagerie.adapter.rest.ListesMessagerieResponses.NouveauxMessagesResponse;
import fr.bnia.messagerie.api.TypeObjet;
import fr.bnia.messagerie.application.ConsulterMessagerie;
import fr.bnia.messagerie.application.MessagerieLecture.Filtre;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Les listes de la messagerie (spec 15 §6) : au back-office, de tous les opérateurs ; au portail, de l'opérateur
 * actif. Lire une liste ne lit aucun message (RG-MSG-10, 11, 15, 16).
 */
@RestController
@Tag(name = "messagerie")
class ListesMessagerieController {

    private final ConsulterMessagerie consulter;

    ListesMessagerieController(ConsulterMessagerie consulter) {
        this.consulter = consulter;
    }

    @GetMapping("/api/messagerie/nouveaux")
    @Operation(operationId = "listerNouveauxMessagesBnia",
            summary = "Back-office : les messages non lus du BNIA, de tous les opérateurs (RG-MSG-11)")
    NouveauxMessagesResponse nouveauxDuBnia(Connecte connecte) {
        return NouveauxMessagesResponse.depuis(consulter.nouveauxDuBnia(Acteurs.de(connecte)));
    }

    @GetMapping("/api/operateurs/{operateur}/messagerie/nouveaux")
    @Operation(operationId = "listerMesNouveauxMessages",
            summary = "Portail : les messages non lus de l'opérateur actif que je peux lire (RG-MSG-11)")
    NouveauxMessagesResponse nouveauxDuPortail(Connecte connecte, @PathVariable long operateur) {
        return NouveauxMessagesResponse.depuis(consulter.nouveauxDuPortail(Acteurs.de(connecte), operateur));
    }

    @GetMapping("/api/messagerie/conversations")
    @Operation(operationId = "listerConversationsBnia",
            summary = "Back-office : les conversations de tous les opérateurs, filtrées et cherchées (RG-MSG-15, 16)")
    List<ConversationListeeResponse> conversationsDuBnia(Connecte connecte,
            @RequestParam(required = false) Long operateur,
            @Parameter(description = "Type d'objet") @RequestParam(required = false) TypeObjet type,
            @Parameter(description = "Sujets libres seulement") @RequestParam(required = false) Boolean libres,
            @RequestParam(required = false) Boolean nonLues,
            @Parameter(description = "Texte cherché, sans casse ni accents") @RequestParam(required = false) String recherche) {
        return ConversationListeeResponse.depuis(consulter.conversationsDuBnia(Acteurs.de(connecte),
                filtre(operateur, type, libres, nonLues, recherche)));
    }

    @GetMapping("/api/operateurs/{operateur}/messagerie/conversations")
    @Operation(operationId = "listerMesConversations",
            summary = "Portail : les conversations de l'opérateur actif que je peux lire, filtrées et cherchées (RG-MSG-15, 16)")
    List<ConversationListeeResponse> conversationsDuPortail(Connecte connecte, @PathVariable long operateur,
            @Parameter(description = "Type d'objet") @RequestParam(required = false) TypeObjet type,
            @Parameter(description = "Sujets libres seulement") @RequestParam(required = false) Boolean libres,
            @RequestParam(required = false) Boolean nonLues,
            @Parameter(description = "Texte cherché, sans casse ni accents") @RequestParam(required = false) String recherche) {
        return ConversationListeeResponse.depuis(consulter.conversationsDuPortail(Acteurs.de(connecte), operateur,
                filtre(null, type, libres, nonLues, recherche)));
    }

    private static Filtre filtre(Long operateur, TypeObjet type, Boolean libres, Boolean nonLues, String recherche) {
        return new Filtre(Optional.ofNullable(operateur), Optional.ofNullable(type), Boolean.TRUE.equals(libres),
                Boolean.TRUE.equals(nonLues), Optional.ofNullable(recherche));
    }
}
