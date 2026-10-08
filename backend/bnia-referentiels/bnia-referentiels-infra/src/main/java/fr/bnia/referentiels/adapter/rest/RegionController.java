package fr.bnia.referentiels.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.referentiels.application.FiltreValeurs;
import fr.bnia.referentiels.application.GererRegions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/referentiels/regions")
@Tag(name = "regions", description = "Référentiel : régions (C.10.6)")
class RegionController {

    private final GererRegions gerer;

    RegionController(GererRegions gerer) {
        this.gerer = gerer;
    }

    @GetMapping
    @Operation(operationId = "listerRegions", summary = "Lister les régions, triées par libellé")
    List<RegionResponse> lister(
            @Parameter(description = "Absent : actives et inactives") @RequestParam(required = false) Boolean actif,
            @Parameter(description = "Texte cherché dans le code ou le libellé") @RequestParam(required = false) String recherche) {
        return gerer.lister(new FiltreValeurs(actif, recherche)).stream().map(RegionResponse::depuis).toList();
    }

    @GetMapping("/{id}")
    @Operation(operationId = "consulterRegion", summary = "Consulter une région, active ou non")
    RegionResponse consulter(@PathVariable long id) {
        return RegionResponse.depuis(gerer.consulter(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "creerRegion", summary = "Créer une région (RG-REF-01, RG-REF-03)")
    RegionResponse creer(Connecte connecte, @RequestBody SaisieRegionRequest requete) {
        return RegionResponse.depuis(gerer.creer(connecte.utilisateur(), requete.code(), requete.libelle(), requete.enRegionDelimitee()));
    }

    @PutMapping("/{id}")
    @Operation(operationId = "modifierRegion", summary = "Modifier une région, code compris (RG-REF-01 à 03)")
    RegionResponse modifier(Connecte connecte, @PathVariable long id, @RequestBody SaisieRegionRequest requete) {
        return RegionResponse.depuis(
                gerer.modifier(connecte.utilisateur(), id, requete.code(), requete.libelle(), requete.enRegionDelimitee()));
    }

    @PostMapping("/{id}/desactivation")
    @Operation(operationId = "desactiverRegion", summary = "Désactiver une région (RG-REF-04)")
    RegionResponse desactiver(Connecte connecte, @PathVariable long id) {
        return RegionResponse.depuis(gerer.desactiver(connecte.utilisateur(), id));
    }

    @PostMapping("/{id}/reactivation")
    @Operation(operationId = "reactiverRegion", summary = "Réactiver une région (RG-REF-04)")
    RegionResponse reactiver(Connecte connecte, @PathVariable long id) {
        return RegionResponse.depuis(gerer.reactiver(connecte.utilisateur(), id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "supprimerRegion", summary = "Supprimer une région qu'aucune autre donnée n'utilise (RG-REF-04)")
    void supprimer(Connecte connecte, @PathVariable long id) {
        gerer.supprimer(connecte.utilisateur(), id);
    }
}
