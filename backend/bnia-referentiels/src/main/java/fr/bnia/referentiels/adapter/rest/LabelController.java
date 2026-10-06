package fr.bnia.referentiels.adapter.rest;

import fr.bnia.commun.adapter.rest.Ressource;
import fr.bnia.referentiels.application.GererLabels;
import fr.bnia.referentiels.domain.Label;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/referentiels/labels")
@Tag(name = "labels", description = "Référentiel : labels (C.10.8.3)")
@Ressource(singulier = "Label", pluriel = "Labels")
class LabelController extends ValeursSimplesController<Label> {

    LabelController(GererLabels gerer) {
        super(gerer);
    }
}
