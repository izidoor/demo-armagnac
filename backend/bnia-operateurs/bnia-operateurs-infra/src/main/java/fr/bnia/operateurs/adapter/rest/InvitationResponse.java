package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.domain.IdentitePersonne;
import fr.bnia.operateurs.domain.Invitation;
import fr.bnia.operateurs.domain.StatutInvitation;
import fr.bnia.operateurs.domain.partage.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "Invitation", description = "Invitation à accéder à un opérateur (spec 02 §3.2.5) ; le jeton n'est jamais renvoyé")
public record InvitationResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long operateur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String email,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Role role,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) StatutInvitation statut,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant expireLe,
        @Schema(description = "Fiche du contact invité ; absente pour une personne nouvelle") Long fiche,
        @Schema(description = "Personne nouvelle : identifiant du titre") Long titre,
        @Schema(description = "Personne nouvelle") String nom,
        @Schema(description = "Personne nouvelle") String prenom,
        String fonction,
        String telephoneProfessionnel,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String envoyeePar,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant envoyeeLe) {

    static InvitationResponse depuis(Invitation i) {
        IdentitePersonne p = i.personne().orElse(null);
        return new InvitationResponse(i.id(), i.operateur(), i.email().valeur(), i.role(), i.statut(), i.expireLe(),
                i.fiche().orElse(null), p == null ? null : p.titre(), p == null ? null : p.nom(),
                p == null ? null : p.prenom(), i.fonction(), i.telephoneProfessionnel(), i.envoyeePar(), i.envoyeeLe());
    }
}
