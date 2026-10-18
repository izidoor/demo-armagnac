package fr.bnia.messagerie.adapter.rest;

import fr.bnia.messagerie.api.TypeObjet;
import fr.bnia.messagerie.application.DroitsMessagerie.OperateurNomme;
import fr.bnia.messagerie.application.VueConversation;
import fr.bnia.messagerie.domain.Conversation;
import fr.bnia.messagerie.domain.Cote;
import fr.bnia.messagerie.domain.ObjetMetier;
import fr.bnia.messagerie.domain.SujetLibre;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "Conversation", description = "Une conversation telle que l'utilisateur la voit (spec 15 §6)")
public record ConversationResponse(
        @Schema(description = "Vide pour un objet qui n'a pas encore de conversation (RG-MSG-04)") Long id,
        @Schema(description = "L'objet métier ; vide pour un sujet libre") ObjetResponse objet,
        @Schema(description = "Titre du sujet libre ; vide pour un objet") String titre,
        @Schema(description = "Auteur du sujet libre ; vide pour un objet") String auteur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) OperateurResponse operateur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Mon côté (RG-MSG-03)") Cote cote,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean peutEcrire,
        @Schema(description = "Pourquoi je n'y écris pas ; vide si j'écris") String lectureSeule,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Sujet libre dont l'auteur n'est plus membre de l'opérateur (RG-MSG-13)") boolean auteurParti,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Du plus ancien au plus récent")
        List<MessageResponse> messages) {

    @Schema(name = "ObjetConversation", description = "L'objet métier d'une conversation (spec 15 §3.2)")
    public record ObjetResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) TypeObjet type,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Identifiant chez son contexte")
            String identifiant,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "DSM octobre 2026") String libelle) {
    }

    @Schema(name = "OperateurConversation", description = "L'opérateur d'une conversation")
    public record OperateurResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String code,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String raisonSociale) {

        static OperateurResponse depuis(OperateurNomme o) {
            return new OperateurResponse(o.id(), o.code(), o.raisonSociale());
        }
    }

    static ConversationResponse depuis(VueConversation v, long utilisateur) {
        ObjetResponse objet = v.sujet() instanceof ObjetMetier o
                ? new ObjetResponse(o.type(), o.objet().identifiant(), o.libelle()) : null;
        SujetLibre libre = v.sujet() instanceof SujetLibre l ? l : null;
        return new ConversationResponse(v.conversation().map(Conversation::id).orElse(null), objet,
                libre == null ? null : libre.titre(), libre == null ? null : libre.auteur().nom(),
                OperateurResponse.depuis(v.nommeOperateur()), v.cote(), v.peutEcrire(), v.lectureSeule().orElse(null),
                v.auteurParti(),
                v.messages().stream().map(m -> MessageResponse.depuis(m.message(), m.nonLu(), utilisateur)).toList());
    }
}
