package fr.bnia.referentiels.adapter.rest;

import fr.bnia.commun.adapter.rest.Ressource;
import fr.bnia.referentiels.application.GererMembresBnia;
import fr.bnia.referentiels.domain.MembreBnia;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/referentiels/membres-bnia")
@Tag(name = "membres-bnia", description = "Référentiel : membres BNIA (C.1.1.1, spec 02)")
@Ressource(singulier = "MembreBnia", pluriel = "MembresBnia")
class MembreBniaController extends ValeursSimplesController<MembreBnia> {

    MembreBniaController(GererMembresBnia gerer) {
        super(gerer);
    }
}
