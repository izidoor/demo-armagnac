package fr.bnia.operateurs.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.operateurs.application.Moi;
import fr.bnia.operateurs.application.MonCompte;
import fr.bnia.operateurs.application.SaisieProfil;
import fr.bnia.operateurs.domain.StatutUtilisateur;
import fr.bnia.operateurs.domain.Utilisateur;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * « Mon compte » (vision UC-21 à 24) : l'utilisateur connecté, ses opérateurs, son profil, son e-mail,
 * son opérateur actif (RG-OPE-33, 47 à 49). Le mot de passe est à {@code authentification}.
 */
@RestController
@Tag(name = "mon-compte", description = "Mon compte : profil, e-mail, opérateurs, opérateur actif")
class MonCompteController {

    private final MonCompte monCompte;

    MonCompteController(MonCompte monCompte) {
        this.monCompte = monCompte;
    }

    @Schema(name = "Moi", description = "L'utilisateur connecté et ses rattachements (RG-OPE-49)")
    record MoiResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
            @Schema(description = "Identifiant du titre") Long titre,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nom,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String prenom,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nomComplet,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String email,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) StatutUtilisateur statut,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "À renvoyer avec le profil (RG-OPE-51)") long version,
            @Schema(description = "Nouvel e-mail en attente de confirmation (RG-OPE-48)") String nouvelEmail,
            @Schema(description = "Expiration du lien de confirmation") Instant nouvelEmailExpireLe,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<UtilisateurResponse.RattachementResponse> rattachements,
            @Schema(description = "Opérateur où l'application s'ouvre ; absent : l'utilisateur choisit (RG-OPE-49)")
            Long operateurOuvert) {

        static MoiResponse depuis(Moi moi) {
            Utilisateur u = moi.utilisateur();
            return new MoiResponse(u.id(), u.identite().titre(), u.identite().nom(), u.identite().prenom(),
                    u.identite().nomComplet(), u.email().valeur(), u.statut(), u.version(),
                    u.changementEmail().map(c -> c.nouvelEmail().valeur()).orElse(null),
                    u.changementEmail().map(c -> c.expireLe()).orElse(null),
                    moi.rattachements().stream().map(UtilisateurResponse.RattachementResponse::depuis).toList(),
                    moi.operateurOuvert().orElse(null));
        }
    }

    @Schema(name = "SaisieProfil", description = "Titre, nom et prénom, modifiés par la personne seule (RG-OPE-47)")
    record SaisieProfilRequest(
            @Schema(description = "Identifiant du titre") Long titre,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nom,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String prenom,
            @Schema(description = "Version lue (RG-OPE-51)") Long version) {
    }

    @Schema(name = "ChangementEmail")
    record ChangementEmailRequest(@Schema(requiredMode = Schema.RequiredMode.REQUIRED) String email) {
    }

    @Schema(name = "ConfirmationEmail")
    record ConfirmationRequest(@Schema(requiredMode = Schema.RequiredMode.REQUIRED) String jeton) {
    }

    @Schema(name = "OperateurActif")
    record OperateurActifRequest(@Schema(requiredMode = Schema.RequiredMode.REQUIRED) long operateur) {
    }

    @GetMapping("/api/moi")
    @Operation(operationId = "consulterMoi", summary = "L'utilisateur connecté, ses rattachements et son opérateur ouvert (RG-OPE-49)")
    MoiResponse moi(Connecte connecte) {
        return MoiResponse.depuis(monCompte.moi(Acteurs.de(connecte)));
    }

    @PutMapping("/api/moi/profil")
    @Operation(operationId = "modifierMonProfil", summary = "Modifier son titre, son nom et son prénom (RG-OPE-27, 47, 51)")
    MoiResponse modifierProfil(Connecte connecte, @RequestBody SaisieProfilRequest requete) {
        monCompte.modifierProfil(Acteurs.de(connecte), new SaisieProfil(requete.titre(), requete.nom(), requete.prenom(),
                requete.version()));
        return moi(connecte);
    }

    @PostMapping("/api/moi/changement-email")
    @Operation(operationId = "demanderChangementEmail",
            summary = "Demander à changer d'e-mail : un lien de confirmation part au nouvel e-mail (RG-OPE-48)")
    MoiResponse demanderChangementEmail(Connecte connecte, @RequestBody ChangementEmailRequest requete) {
        monCompte.demanderChangementEmail(Acteurs.de(connecte), requete.email());
        return moi(connecte);
    }

    @PutMapping("/api/moi/operateur-actif")
    @Operation(operationId = "choisirOperateurActif", summary = "Choisir l'opérateur où travailler (RG-OPE-49)")
    MoiResponse choisirOperateurActif(Connecte connecte, @RequestBody OperateurActifRequest requete) {
        return MoiResponse.depuis(monCompte.choisirOperateurActif(Acteurs.de(connecte), requete.operateur()));
    }

    @PostMapping("/api/changements-email/confirmation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "confirmerChangementEmail",
            summary = "Confirmer un nouvel e-mail par le lien reçu ; l'identifiant de connexion suit (RG-OPE-48, RG-AUT-10)")
    void confirmerChangementEmail(@RequestBody ConfirmationRequest requete) {
        monCompte.confirmerChangementEmail(requete.jeton());
    }
}
