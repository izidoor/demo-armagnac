package fr.bnia.operateurs.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.operateurs.application.ConsulterAnnuaire;
import fr.bnia.operateurs.application.GererMembres;
import fr.bnia.operateurs.domain.TypeCible;
import fr.bnia.operateurs.domain.partage.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Membres d'un opérateur (vision UC-14, 15, 16) : rôle, suspension, retrait ; module Accès (RG-OPE-31). */
@RestController
@RequestMapping("/api/operateurs/{operateur}/membres")
@Tag(name = "membres", description = "Opérateurs : membres (rattachements), rôles, suspensions, retraits")
class MembreController {

    private final GererMembres gerer;
    private final ConsulterAnnuaire annuaire;

    MembreController(GererMembres gerer, ConsulterAnnuaire annuaire) {
        this.gerer = gerer;
        this.annuaire = annuaire;
    }

    @Schema(name = "SaisieRole")
    record SaisieRoleRequest(@Schema(requiredMode = Schema.RequiredMode.REQUIRED) Role role) {
    }

    @Schema(name = "Retrait", description = "Retrait de l'accès d'un membre (RG-OPE-44)")
    record RetraitRequest(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Conserver cette personne dans les contacts")
            boolean conserverContact) {
    }

    @GetMapping("/{membre}")
    @Operation(operationId = "consulterMembre", summary = "Consulter un membre, sa fiche et ses désignations")
    MembreResponse consulter(Connecte connecte, @PathVariable long operateur, @PathVariable long membre) {
        return MembreResponse.depuis(gerer.consulter(Acteurs.de(connecte), operateur, membre));
    }

    @GetMapping("/{membre}/journal")
    @Operation(operationId = "consulterJournalMembre", summary = "Journal d'un membre, du plus récent au plus ancien (RG-OPE-34)")
    List<LigneJournalResponse> consulterJournal(Connecte connecte, @PathVariable long operateur,
            @PathVariable long membre) {
        return annuaire.journalDe(Acteurs.de(connecte), operateur, TypeCible.MEMBRE, membre).stream()
                .map(LigneJournalResponse::depuis).toList();
    }

    @PutMapping("/{membre}/role")
    @Operation(operationId = "changerRoleMembre", summary = "Changer le rôle d'un membre (RG-OPE-35, 43)")
    MembreResponse changerRole(Connecte connecte, @PathVariable long operateur, @PathVariable long membre,
            @RequestBody SaisieRoleRequest requete) {
        gerer.changerRole(Acteurs.de(connecte), operateur, membre, requete.role());
        return consulter(connecte, operateur, membre);
    }

    @PostMapping("/{membre}/suspension")
    @Operation(operationId = "suspendreMembre", summary = "Suspendre l'accès d'un membre à cet opérateur (RG-OPE-35, 43)")
    MembreResponse suspendre(Connecte connecte, @PathVariable long operateur, @PathVariable long membre) {
        gerer.suspendre(Acteurs.de(connecte), operateur, membre);
        return consulter(connecte, operateur, membre);
    }

    @PostMapping("/{membre}/reactivation")
    @Operation(operationId = "reactiverMembre", summary = "Réactiver l'accès d'un membre (RG-OPE-43)")
    MembreResponse reactiver(Connecte connecte, @PathVariable long operateur, @PathVariable long membre) {
        gerer.reactiver(Acteurs.de(connecte), operateur, membre);
        return consulter(connecte, operateur, membre);
    }

    @PostMapping("/{membre}/retrait")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "retirerMembre",
            summary = "Retirer l'accès d'un membre, en le gardant ou non comme contact ; au dernier rattachement, l'utilisateur est supprimé (RG-OPE-35, 44)")
    void retirer(Connecte connecte, @PathVariable long operateur, @PathVariable long membre,
            @RequestBody RetraitRequest requete) {
        gerer.retirer(Acteurs.de(connecte), operateur, membre, requete.conserverContact());
    }
}
