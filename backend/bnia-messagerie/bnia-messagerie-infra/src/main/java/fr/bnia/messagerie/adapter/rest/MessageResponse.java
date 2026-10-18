package fr.bnia.messagerie.adapter.rest;

import fr.bnia.messagerie.domain.Cote;
import fr.bnia.messagerie.domain.Message;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(name = "Message", description = "Message d'une conversation (spec 15 §3.4)")
public record MessageResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Copie du nom de l'auteur") String auteur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Cote cote,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant ecritLe,
        @Schema(description = "Renseigné si modifié (« modifié » affiché, RG-MSG-06)") Instant modifieLe,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String texte,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Motif d'une demande de compléments : ne se supprime pas (RG-MSG-06, 08)") boolean protege,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Non lu de mon côté avant cet affichage, à mettre en évidence (RG-MSG-10)") boolean nonLu,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Écrit par l'utilisateur connecté : il le modifie ou le supprime") boolean deLUtilisateur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<PieceJointeResponse> pieces) {

    static MessageResponse depuis(Message m, boolean nonLu, long utilisateur) {
        return new MessageResponse(m.id(), m.auteur().nom(), m.cote(), m.ecritLe(), m.modifieLe(), m.texte(),
                m.protege(), nonLu, m.auteur().utilisateur() == utilisateur,
                m.pieces().stream().map(PieceJointeResponse::depuis).toList());
    }
}
