package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.FicheContact;
import fr.bnia.operateurs.domain.Contact;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Contact", description = "Un contact, sa fiche professionnelle et ses désignations")
public record ContactResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long operateur,
        @Schema(description = "Identifiant du titre") Long titre,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nom,
        String prenom,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nomComplet,
        String email,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "À renvoyer avec chaque modification (RG-OPE-51)")
        long version,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) FicheResponse fiche,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Tombera à la suppression (RG-OPE-22)")
        boolean contactPrincipal,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Tombera à la suppression (RG-OPE-23)")
        boolean destinataireFactures,
        @Schema(description = "Invitation en attente partie de ce contact (RG-OPE-40)") InvitationResponse invitationEnAttente) {

    static ContactResponse depuis(FicheContact f) {
        Contact c = f.contact();
        return new ContactResponse(c.id(), c.operateur(), c.identite().titre(), c.identite().nom(), c.identite().prenom(),
                c.identite().nomComplet(), c.email() == null ? null : c.email().valeur(), c.version(),
                FicheResponse.depuis(f.fiche()), f.contactPrincipal(), f.destinataireFactures(),
                f.invitationEnAttente().map(InvitationResponse::depuis).orElse(null));
    }
}
