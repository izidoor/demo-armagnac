package fr.bnia.referentiels.adapter.rest;

import fr.bnia.referentiels.application.FiltreValeurs;
import fr.bnia.referentiels.application.GererPays;
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
@RequestMapping("/api/referentiels/pays")
@Tag(name = "pays", description = "Référentiel : pays (C.10.5)")
class PaysController {

    private final GererPays gerer;

    PaysController(GererPays gerer) {
        this.gerer = gerer;
    }

    @GetMapping
    @Operation(operationId = "listerPays", summary = "Lister les pays, triés par libellé")
    List<PaysResponse> lister(
            @Parameter(description = "Absent : actifs et inactifs") @RequestParam(required = false) Boolean actif,
            @Parameter(description = "Texte cherché dans le code, le code ISO ou le libellé") @RequestParam(required = false) String recherche) {
        return gerer.lister(new FiltreValeurs(actif, recherche)).stream().map(PaysResponse::depuis).toList();
    }

    @GetMapping("/{code}")
    @Operation(operationId = "consulterPays", summary = "Consulter un pays, actif ou non")
    PaysResponse consulter(@PathVariable String code) {
        return PaysResponse.depuis(gerer.consulter(code));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "creerPays", summary = "Créer un pays (RG-REF-01, 03, 05 à 09)")
    PaysResponse creer(@RequestBody CreationPaysRequest requete) {
        return PaysResponse.depuis(gerer.creer(requete.code(), requete.versSaisie()));
    }

    @PutMapping("/{code}")
    @Operation(operationId = "modifierPays", summary = "Modifier un pays ; le code est immuable (RG-REF-02)")
    PaysResponse modifier(@PathVariable String code, @RequestBody ModificationPaysRequest requete) {
        return PaysResponse.depuis(gerer.modifier(code, requete.versSaisie()));
    }

    @PostMapping("/{code}/desactivation")
    @Operation(operationId = "desactiverPays", summary = "Désactiver un pays (RG-REF-04)")
    PaysResponse desactiver(@PathVariable String code) {
        return PaysResponse.depuis(gerer.desactiver(code));
    }

    @PostMapping("/{code}/reactivation")
    @Operation(operationId = "reactiverPays", summary = "Réactiver un pays (RG-REF-04)")
    PaysResponse reactiver(@PathVariable String code) {
        return PaysResponse.depuis(gerer.reactiver(code));
    }
}
