package fr.bnia.referentiels.adapter.rest;

import fr.bnia.commun.adapter.rest.Ressource;
import fr.bnia.referentiels.application.GererTitres;
import fr.bnia.referentiels.domain.Titre;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/referentiels/titres")
@Tag(name = "titres", description = "Référentiel : titres (C.10.8.1)")
@Ressource(singulier = "Titre", pluriel = "Titres")
class TitreController extends ValeursSimplesController<Titre> {

    TitreController(GererTitres gerer) {
        super(gerer);
    }
}
