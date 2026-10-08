package fr.bnia.referentiels.adapter.rest;

import fr.bnia.commun.adapter.rest.Ressource;
import fr.bnia.referentiels.application.GererTypesVisite;
import fr.bnia.referentiels.domain.TypeVisite;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/referentiels/types-visite")
@Tag(name = "types-visite", description = "Référentiel : types de visite (C.10.8.4)")
@Ressource(singulier = "TypeVisite", pluriel = "TypesVisite")
class TypeVisiteController extends ValeursSimplesController<TypeVisite> {

    TypeVisiteController(GererTypesVisite gerer) {
        super(gerer);
    }
}
