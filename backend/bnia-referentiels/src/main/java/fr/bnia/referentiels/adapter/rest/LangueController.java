package fr.bnia.referentiels.adapter.rest;

import fr.bnia.commun.adapter.rest.Ressource;
import fr.bnia.referentiels.application.GererLangues;
import fr.bnia.referentiels.domain.Langue;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/referentiels/langues")
@Tag(name = "langues", description = "Référentiel : langues (C.10.8.5)")
@Ressource(singulier = "Langue", pluriel = "Langues")
class LangueController extends ValeursSimplesController<Langue> {

    LangueController(GererLangues gerer) {
        super(gerer);
    }
}
