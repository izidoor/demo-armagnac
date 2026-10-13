package fr.bnia.declarations.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.declarations.application.GererPointDeDepart;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "fixerPointDeDepart",
            summary = "Le BNIA fixe le point de départ d'un producteur ou d'un négociant (RG-DEC-01, 06, 07)")
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
