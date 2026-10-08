package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.PersonneAnnuaire;
import fr.bnia.operateurs.domain.StatutRattachement;
import fr.bnia.operateurs.domain.partage.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PersonneAnnuaire", description = "Ligne de l'annuaire : contact, membre, ou personne nouvelle invitée (RG-OPE-52)")
public record PersonneAnnuaireResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) PersonneAnnuaire.Genre genre,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Contact, rattachement ou invitation, selon le genre")
        long id,
        @Schema(description = "Fiche professionnelle ; absente pour une invitation") Long fiche,
        @Schema(description = "Identifiant du titre") Long titre,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nom,
        String prenom,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nomComplet,
        String email,
        String fonction,
        String telephoneProfessionnel,
        @Schema(description = "Rôle du membre, ou rôle proposé par l'invitation") Role role,
        @Schema(description = "Membre : ACTIF ou SUSPENDU (« accès suspendu »)") StatutRattachement statut,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Membre bloqué par le BNIA (RG-OPE-50)")
        boolean bloqueParBnia,
        @Schema(description = "Invitation en attente (« invitation envoyée »)") Long invitationEnAttente) {

    static PersonneAnnuaireResponse depuis(PersonneAnnuaire p) {
        String nom = p.nom() == null ? p.email() : p.nom();
        return new PersonneAnnuaireResponse(p.genre(), p.id(), p.fiche(), p.titre(), nom, p.prenom(),
                p.prenom() == null ? nom : p.prenom() + " " + nom, p.email(), p.fonction(), p.telephoneProfessionnel(),
                p.role(), p.statut(), p.bloqueParBnia(), p.invitationEnAttente());
    }
}
