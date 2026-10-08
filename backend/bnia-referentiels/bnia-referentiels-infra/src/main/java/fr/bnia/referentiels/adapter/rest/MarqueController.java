package fr.bnia.referentiels.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.commun.adapter.rest.Ressource;
import fr.bnia.referentiels.application.GererMarques;
import fr.bnia.referentiels.domain.Marque;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    @PostMapping("/fiche/{operateur}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "creerMarqueDepuisFiche",
            summary = "Créer une marque depuis la fiche d'un opérateur, par qui gère ses contacts (RG-REF-14, RG-REF-15)")
    ValeurSimpleResponse creerDepuisFiche(Connecte connecte, @PathVariable long operateur,
            @RequestBody SaisieValeurSimpleRequest requete) {
        return ValeurSimpleResponse.depuis(gerer.creerDepuisFiche(connecte.utilisateur(), operateur, requete.code(),
                requete.libelle()));
    }

    @GetMapping("/proches")
    @Operation(operationId = "listerMarquesProches",
            summary = "Marques au libellé proche d'un texte saisi, actives ou non (RG-REF-15)")
    List<ValeurSimpleResponse> proches(@RequestParam String libelle) {
        return gerer.proches(libelle).stream().map(ValeurSimpleResponse::depuis).toList();
    }
}
