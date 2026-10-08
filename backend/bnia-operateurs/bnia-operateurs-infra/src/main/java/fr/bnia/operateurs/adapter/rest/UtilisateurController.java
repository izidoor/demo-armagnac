package fr.bnia.operateurs.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.operateurs.application.FiltreUtilisateurs;
import fr.bnia.operateurs.application.GererUtilisateurs;
import fr.bnia.operateurs.domain.StatutUtilisateur;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Utilisateurs, au back-office (menu Accès) : liste (RG-OPE-53), blocage par un ADMIN du BNIA (RG-OPE-50). */
@RestController
@RequestMapping("/api/utilisateurs")
@Tag(name = "utilisateurs", description = "Back-office : utilisateurs, blocage par le BNIA")
class UtilisateurController {

    private final GererUtilisateurs gerer;

    UtilisateurController(GererUtilisateurs gerer) {
        this.gerer = gerer;
    }

    @Schema(name = "Blocage")
    record BlocageRequest(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Conservé au journal, jamais envoyé (RG-OPE-50)")
            String motif) {
    }

    @GetMapping
    @Operation(operationId = "listerUtilisateurs", summary = "Lister les utilisateurs et leurs rattachements (RG-OPE-32, 53)")
    List<UtilisateurResponse> lister(Connecte connecte,
            @Parameter(description = "Contenu dans le nom, le prénom ou l'e-mail") @RequestParam(required = false) String recherche,
            @RequestParam(required = false) StatutUtilisateur statut,
            @Parameter(description = "Un rattachement à cet opérateur") @RequestParam(required = false) Long operateur) {
        return gerer.lister(Acteurs.de(connecte), new FiltreUtilisateurs(recherche, statut, operateur)).stream()
                .map(UtilisateurResponse::depuis).toList();
    }

    @GetMapping("/{id}")
    @Operation(operationId = "consulterUtilisateur", summary = "Consulter un utilisateur et ses rattachements (RG-OPE-53)")
    UtilisateurResponse consulter(Connecte connecte, @PathVariable long id) {
        return UtilisateurResponse.depuis(gerer.consulter(Acteurs.de(connecte), id));
    }

    @PostMapping("/{id}/blocage")
    @Operation(operationId = "bloquerUtilisateur",
            summary = "Bloquer un utilisateur dans tous les opérateurs, avec un motif ; il est prévenu, et les ADMIN de ses opérateurs (RG-OPE-50)")
    UtilisateurResponse bloquer(Connecte connecte, @PathVariable long id, @RequestBody BlocageRequest requete) {
        gerer.bloquer(Acteurs.de(connecte), id, requete.motif());
        return consulter(connecte, id);
    }

    @PostMapping("/{id}/deblocage")
    @Operation(operationId = "debloquerUtilisateur", summary = "Débloquer un utilisateur (RG-OPE-50)")
    UtilisateurResponse debloquer(Connecte connecte, @PathVariable long id) {
        gerer.debloquer(Acteurs.de(connecte), id);
        return consulter(connecte, id);
    }
}
