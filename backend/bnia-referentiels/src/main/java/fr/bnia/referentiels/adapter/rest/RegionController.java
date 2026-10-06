package fr.bnia.referentiels.adapter.rest;

import fr.bnia.referentiels.application.FiltreValeurs;
import fr.bnia.referentiels.application.GererRegions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/{code}")
    @Operation(operationId = "consulterRegion", summary = "Consulter une région, active ou non")
    RegionResponse consulter(@PathVariable String code) {
        return RegionResponse.depuis(gerer.consulter(code));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "creerRegion", summary = "Créer une région (RG-REF-01, RG-REF-03)")
    RegionResponse creer(@RequestBody CreationRegionRequest requete) {
        return RegionResponse.depuis(gerer.creer(requete.code(), requete.libelle(), requete.enRegionDelimitee()));
    }

    @PutMapping("/{code}")
    @Operation(operationId = "modifierRegion", summary = "Modifier une région ; le code est immuable (RG-REF-02)")
    RegionResponse modifier(@PathVariable String code, @RequestBody ModificationRegionRequest requete) {
        return RegionResponse.depuis(gerer.modifier(code, requete.libelle(), requete.enRegionDelimitee()));
    }

    @PostMapping("/{code}/desactivation")
    @Operation(operationId = "desactiverRegion", summary = "Désactiver une région (RG-REF-04)")
    RegionResponse desactiver(@PathVariable String code) {
        return RegionResponse.depuis(gerer.desactiver(code));
    }

    @PostMapping("/{code}/reactivation")
    @Operation(operationId = "reactiverRegion", summary = "Réactiver une région (RG-REF-04)")
    RegionResponse reactiver(@PathVariable String code) {
        return RegionResponse.depuis(gerer.reactiver(code));
    }
}
