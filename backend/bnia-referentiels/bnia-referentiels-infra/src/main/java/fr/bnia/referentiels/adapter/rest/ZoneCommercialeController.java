package fr.bnia.referentiels.adapter.rest;

import fr.bnia.commun.adapter.rest.Ressource;
import fr.bnia.referentiels.application.GererZonesCommerciales;
import fr.bnia.referentiels.domain.ZoneCommerciale;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/referentiels/zones-commerciales")
@Tag(name = "zones-commerciales", description = "Référentiel : zones commerciales, continents (C.10.4)")
@Ressource(singulier = "ZoneCommerciale", pluriel = "ZonesCommerciales")
class ZoneCommercialeController extends ValeursSimplesController<ZoneCommerciale> {

    ZoneCommercialeController(GererZonesCommerciales gerer) {
        super(gerer);
    }
}
