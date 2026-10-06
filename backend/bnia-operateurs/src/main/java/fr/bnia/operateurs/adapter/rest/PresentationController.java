package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.GererPresentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Présentation de l'opérateur au back-office (C.1.4 à C.1.7). */
@RestController
@RequestMapping("/api/operateurs/{operateur}/presentation")
@Tag(name = "presentation", description = "Opérateurs : marques, circuits, visites, labels, œnotourisme, médailles")
class PresentationController {

    private final GererPresentation gerer;

    PresentationController(GererPresentation gerer) {
        this.gerer = gerer;
    }

    @GetMapping
    @Operation(operationId = "consulterPresentationOperateur", summary = "Consulter la présentation ; vide tant que rien n'est saisi")
    PresentationDto consulter(@PathVariable long operateur) {
        return PresentationDto.depuis(gerer.consulter(operateur));
    }

    @PutMapping
    @Operation(operationId = "modifierPresentationOperateur", summary = "Remplacer la présentation (RG-OPE-16 à 19, 21)")
    PresentationDto modifier(@PathVariable long operateur, @RequestBody PresentationDto requete) {
        return PresentationDto.depuis(gerer.modifier(operateur, requete.versSaisie()));
    }
}
