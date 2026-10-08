package fr.bnia.operateurs.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.operateurs.application.GererFiches;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Fiches professionnelles (vision UC-18, 25) : la sienne, ou par le module Contacts (RG-OPE-46). */
@RestController
@RequestMapping("/api/operateurs/{operateur}/fiches")
@Tag(name = "fiches", description = "Opérateurs : fiches professionnelles des contacts et des membres")
class FicheController {

    private final GererFiches gerer;

    FicheController(GererFiches gerer) {
        this.gerer = gerer;
    }

    @GetMapping("/{fiche}")
    @Operation(operationId = "consulterFiche", summary = "Consulter une fiche professionnelle")
    FicheResponse consulter(Connecte connecte, @PathVariable long operateur, @PathVariable long fiche) {
        return FicheResponse.depuis(gerer.consulter(Acteurs.de(connecte), operateur, fiche));
    }

    @PutMapping("/{fiche}")
    @Operation(operationId = "modifierFiche",
            summary = "Modifier une fiche professionnelle : la sienne, ou par le module Contacts (RG-OPE-21, 33, 46, 51)")
    FicheResponse modifier(Connecte connecte, @PathVariable long operateur, @PathVariable long fiche,
            @RequestBody SaisieFicheRequest requete) {
        return FicheResponse.depuis(gerer.modifier(Acteurs.de(connecte), operateur, fiche, requete.versSaisie()));
    }
}
