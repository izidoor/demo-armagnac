package fr.bnia.operateurs.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.operateurs.application.Acceptation;
import fr.bnia.operateurs.application.GererInvitations;
import fr.bnia.operateurs.application.InvitationPublique;
import fr.bnia.operateurs.application.SaisieAcceptation;
import fr.bnia.operateurs.domain.partage.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Le lien d'une invitation (vision UC-20) : ouvert sans session. Une personne nouvelle accepte sans être
 * connectée, puis pose son mot de passe auprès de {@code authentification} avec le même jeton
 * (RG-AUT-15) ; un compte existant se connecte d'abord.
 */
@RestController
@RequestMapping("/api/invitations/{jeton}")
@Tag(name = "acceptation", description = "Accepter une invitation (lien de l'e-mail)")
class InvitationPubliqueController {

    private final GererInvitations gerer;

    InvitationPubliqueController(GererInvitations gerer) {
        this.gerer = gerer;
    }

    @Schema(name = "InvitationPublique", description = "Ce que montre le lien d'une invitation")
    record InvitationPubliqueResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String operateur,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Role role,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String email,
            @Schema(description = "Prérempli : identifiant du titre") Long titre,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Prérempli") String nom,
            @Schema(description = "Prérempli") String prenom,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Vrai : la personne se connecte pour accepter")
            boolean compteExistant,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant expireLe) {

        static InvitationPubliqueResponse depuis(InvitationPublique i) {
            return new InvitationPubliqueResponse(i.operateur(), i.role(), i.email(), i.titre(), i.nom(), i.prenom(),
                    i.compteExistant(), i.expireLe());
        }
    }

    @Schema(name = "SaisieAcceptation", description = "Identité confirmée par une personne nouvelle (RG-OPE-42)")
    record SaisieAcceptationRequest(
            @Schema(description = "Identifiant du titre") Long titre,
            @Schema(description = "Obligatoire pour une personne nouvelle") String nom,
            @Schema(description = "Obligatoire pour une personne nouvelle") String prenom) {
    }

    @Schema(name = "Acceptation")
    record AcceptationResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long operateur,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                    description = "Vrai : le compte vient d'être créé, sans mot de passe ; le poser avec le même jeton (RG-AUT-15)")
            boolean nouveauCompte) {
    }

    @GetMapping
    @Operation(operationId = "consulterInvitationParJeton",
            summary = "L'opérateur, le rôle et l'identité préremplie d'une invitation acceptable (RG-OPE-42)")
    InvitationPubliqueResponse consulter(@PathVariable String jeton) {
        return InvitationPubliqueResponse.depuis(gerer.consulterParJeton(jeton));
    }

    @PostMapping("/acceptation")
    @Operation(operationId = "accepterInvitation",
            summary = "Accepter : utilisateur créé ou retrouvé (connecté), fiche, rattachement actif (RG-OPE-29, 30, 42, 49)")
    AcceptationResponse accepter(Optional<Connecte> connecte, @PathVariable String jeton,
            @RequestBody SaisieAcceptationRequest requete) {
        Acceptation acceptation = gerer.accepter(jeton, connecte.map(Acteurs::de),
                new SaisieAcceptation(requete.titre(), requete.nom(), requete.prenom()));
        return new AcceptationResponse(acceptation.operateur(), acceptation.nouveauCompte());
    }
}
