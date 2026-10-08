package fr.bnia.referentiels.adapter.rest;

import fr.bnia.commun.adapter.rest.Ressource;
import fr.bnia.referentiels.application.GererProduitsCommercialises;
import fr.bnia.referentiels.domain.ProduitCommercialise;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/referentiels/produits-commercialises")
@Tag(name = "produits-commercialises", description = "Référentiel : produits commercialisés (C.10.9.3)")
@Ressource(singulier = "ProduitCommercialise", pluriel = "ProduitsCommercialises")
class ProduitCommercialiseController extends ValeursSimplesController<ProduitCommercialise> {

    ProduitCommercialiseController(GererProduitsCommercialises gerer) {
        super(gerer);
    }
}
