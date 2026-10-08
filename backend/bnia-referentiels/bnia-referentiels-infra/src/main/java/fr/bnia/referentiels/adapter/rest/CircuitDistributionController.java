package fr.bnia.referentiels.adapter.rest;

import fr.bnia.commun.adapter.rest.Ressource;
import fr.bnia.referentiels.application.GererCircuitsDistribution;
import fr.bnia.referentiels.domain.CircuitDistribution;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/referentiels/circuits-distribution")
@Tag(name = "circuits-distribution", description = "Référentiel : circuits de distribution (C.10.9.2)")
@Ressource(singulier = "CircuitDistribution", pluriel = "CircuitsDistribution")
class CircuitDistributionController extends ValeursSimplesController<CircuitDistribution> {

    CircuitDistributionController(GererCircuitsDistribution gerer) {
        super(gerer);
    }
}
