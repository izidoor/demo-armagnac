package fr.bnia.operateurs.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.operateurs.application.GererInvitations;
import fr.bnia.operateurs.application.SaisieInvitation;
import fr.bnia.operateurs.domain.partage.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Invitations d'un opérateur (vision UC-02, 12, 13), module Accès (RG-OPE-31). Le jeton part par e-mail seulement. */
@RestController
@RequestMapping("/api/operateurs/{operateur}/invitations")
@Tag(name = "invitations", description = "Opérateurs : invitations à accéder à l'application")
class InvitationController {

    private final GererInvitations gerer;

    InvitationController(GererInvitations gerer) {
        this.gerer = gerer;
    }

    @Schema(name = "SaisieInvitation", description = "Depuis un contact, ou une personne nouvelle (RG-OPE-40)")
    record SaisieInvitationRequest(
            @Schema(description = "Contact invité ; absent pour une personne nouvelle") Long contact,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "marc.lafitte@exemple.fr") String email,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Role role,
            @Schema(description = "Personne nouvelle : identifiant du titre") Long titre,
            @Schema(description = "Personne nouvelle : obligatoire") String nom,
            @Schema(description = "Personne nouvelle : obligatoire") String prenom,
            String fonction,
            String telephoneProfessionnel) {

        SaisieInvitation versSaisie() {
            return new SaisieInvitation(contact, email, role, titre, nom, prenom, fonction, telephoneProfessionnel);
        }
    }

    @GetMapping("/{invitation}")
    @Operation(operationId = "consulterInvitation", summary = "Consulter une invitation")
    InvitationResponse consulter(Connecte connecte, @PathVariable long operateur, @PathVariable long invitation) {
        return InvitationResponse.depuis(gerer.consulter(Acteurs.de(connecte), operateur, invitation));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "inviter",
            summary = "Inviter un contact ou une personne nouvelle ; le lien part par e-mail (RG-OPE-28, 29, 40)")
    InvitationResponse inviter(Connecte connecte, @PathVariable long operateur,
            @RequestBody SaisieInvitationRequest requete) {
        return InvitationResponse.depuis(gerer.inviter(Acteurs.de(connecte), operateur, requete.versSaisie()).invitation());
    }

    @PostMapping("/{invitation}/relance")
    @Operation(operationId = "relancerInvitation", summary = "Relancer : nouveau lien, nouvelle expiration (RG-OPE-41)")
    InvitationResponse relancer(Connecte connecte, @PathVariable long operateur, @PathVariable long invitation) {
        return InvitationResponse.depuis(gerer.relancer(Acteurs.de(connecte), operateur, invitation).invitation());
    }

    @PostMapping("/{invitation}/annulation")
    @Operation(operationId = "annulerInvitation", summary = "Annuler une invitation en attente (RG-OPE-41)")
    InvitationResponse annuler(Connecte connecte, @PathVariable long operateur, @PathVariable long invitation) {
        gerer.annuler(Acteurs.de(connecte), operateur, invitation);
        return consulter(connecte, operateur, invitation);
    }
}
