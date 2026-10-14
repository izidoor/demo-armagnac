package fr.bnia.declarations.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.declarations.application.GererPointDeDepart;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Point de départ déclaratif d'un opérateur (analyse UC-DEC-3, spec 05 §6.1), module Déclarations. */
@RestController
@RequestMapping("/api/operateurs/{operateur}/point-de-depart")
@Tag(name = "declarations", description = "Déclarations de stocks et mouvements (spec 05)")
class PointDeDepartController {

    private final GererPointDeDepart gerer;

    PointDeDepartController(GererPointDeDepart gerer) {
        this.gerer = gerer;
    }

    @GetMapping
    @Operation(operationId = "consulterPointDeDepart",
            summary = "Le point de départ en vigueur de l'opérateur ; 204 s'il n'est pas fixé (RG-DEC-01)")
    ResponseEntity<PointDeDepartResponse> consulter(Connecte connecte, @PathVariable long operateur) {
        return gerer.consulter(Acteurs.de(connecte), operateur).map(PointDeDepartResponse::depuis)
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/historique")
    @Operation(operationId = "historiquePointsDeDepart",
            summary = "Tous les points de départ de l'opérateur, du plus récent au plus ancien (RG-DEC-01, 09)")
    List<PointDeDepartResponse> historique(Connecte connecte, @PathVariable long operateur) {
        return gerer.historique(Acteurs.de(connecte), operateur).stream().map(PointDeDepartResponse::depuis).toList();
    }

    @GetMapping("/proposition")
    @Operation(operationId = "proposerPointDeDepart",
            summary = "Le stock proposé pour un nouveau point de départ, vieilli à la date retenue ; 204 sans DSM validée (RG-DEC-01, 09, 14)")
    ResponseEntity<PropositionPointDeDepartResponse> proposer(Connecte connecte, @PathVariable long operateur,
            @Parameter(description = "Date de début ; vide : le lendemain de la dernière DSM validée")
            @RequestParam(required = false) LocalDate debut) {
        return gerer.proposer(Acteurs.de(connecte), operateur, Optional.ofNullable(debut))
                .map(PropositionPointDeDepartResponse::depuis)
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "fixerPointDeDepart",
            summary = "Le BNIA fixe le premier point de départ d'un producteur ou d'un négociant, ou un nouveau quand le précédent est figé (RG-DEC-01, 06, 07, 09)")
    PointDeDepartResponse fixer(Connecte connecte, @PathVariable long operateur,
            @RequestBody SaisiePointDeDepartRequest requete) {
        return PointDeDepartResponse.depuis(gerer.fixer(Acteurs.de(connecte), operateur, requete.debut(),
                requete.versSaisie()));
    }

    @PutMapping("/{id}")
    @Operation(operationId = "modifierPointDeDepart",
            summary = "Le BNIA modifie le point de départ tant qu'il n'est pas figé ; la DSM en cours suit (RG-DEC-01, 07, 08)")
    PointDeDepartResponse modifier(Connecte connecte, @PathVariable long operateur, @PathVariable long id,
            @RequestBody SaisiePointDeDepartRequest requete) {
        return PointDeDepartResponse.depuis(gerer.modifier(Acteurs.de(connecte), operateur, id, requete.debut(),
                requete.versSaisie(), requete.version()));
    }
}
