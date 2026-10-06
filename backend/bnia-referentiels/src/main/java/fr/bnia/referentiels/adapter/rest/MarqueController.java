package fr.bnia.referentiels.adapter.rest;

import fr.bnia.commun.adapter.rest.Ressource;
import fr.bnia.referentiels.application.GererMarques;
import fr.bnia.referentiels.domain.Marque;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Marques. La création sert au paramétrage comme à la fiche opérateur, au back-office et au portail
 * (RG-REF-14).
 */
@RestController
@RequestMapping("/api/referentiels/marques")
@Tag(name = "marques", description = "Référentiel : marques (C.10.9.1)")
@Ressource(singulier = "Marque", pluriel = "Marques")
class MarqueController extends ValeursSimplesController<Marque> {

    private final GererMarques gerer;

    MarqueController(GererMarques gerer) {
        super(gerer);
        this.gerer = gerer;
    }

    @GetMapping("/proches")
    @Operation(operationId = "listerMarquesProches",
            summary = "Marques au libellé proche d'un texte saisi, actives ou non (RG-REF-15)")
    List<ValeurSimpleResponse> proches(@RequestParam String libelle) {
        return gerer.proches(libelle).stream().map(ValeurSimpleResponse::depuis).toList();
    }
}
