package fr.bnia.referentiels.adapter.rest;

import fr.bnia.commun.adapter.rest.Ressource;
import fr.bnia.referentiels.application.GererCentresInteret;
import fr.bnia.referentiels.domain.CentreInteret;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/referentiels/centres-interet")
@Tag(name = "centres-interet", description = "Référentiel : centres d'intérêt (C.10.8.6)")
@Ressource(singulier = "CentreInteret", pluriel = "CentresInteret")
class CentreInteretController extends ValeursSimplesController<CentreInteret> {

    CentreInteretController(GererCentresInteret gerer) {
        super(gerer);
    }
}
