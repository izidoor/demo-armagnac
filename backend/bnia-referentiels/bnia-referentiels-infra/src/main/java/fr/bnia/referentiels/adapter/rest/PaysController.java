package fr.bnia.referentiels.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.referentiels.application.FiltreValeurs;
import fr.bnia.referentiels.application.GererPays;
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

    @GetMapping("/{id}")
    @Operation(operationId = "consulterPays", summary = "Consulter un pays, actif ou non")
    PaysResponse consulter(@PathVariable long id) {
        return PaysResponse.depuis(gerer.consulter(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "creerPays", summary = "Créer un pays (RG-REF-01, 03, 05 à 09)")
    PaysResponse creer(Connecte connecte, @RequestBody SaisiePaysRequest requete) {
        return PaysResponse.depuis(gerer.creer(connecte.utilisateur(), requete.versSaisie()));
    }

    @PutMapping("/{id}")
    @Operation(operationId = "modifierPays", summary = "Modifier un pays, code compris (RG-REF-01 à 03, 05 à 09)")
    PaysResponse modifier(Connecte connecte, @PathVariable long id, @RequestBody SaisiePaysRequest requete) {
        return PaysResponse.depuis(gerer.modifier(connecte.utilisateur(), id, requete.versSaisie()));
    }

    @PostMapping("/{id}/desactivation")
    @Operation(operationId = "desactiverPays", summary = "Désactiver un pays (RG-REF-04)")
    PaysResponse desactiver(Connecte connecte, @PathVariable long id) {
        return PaysResponse.depuis(gerer.desactiver(connecte.utilisateur(), id));
    }

    @PostMapping("/{id}/reactivation")
    @Operation(operationId = "reactiverPays", summary = "Réactiver un pays (RG-REF-04)")
    PaysResponse reactiver(Connecte connecte, @PathVariable long id) {
        return PaysResponse.depuis(gerer.reactiver(connecte.utilisateur(), id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "supprimerPays", summary = "Supprimer un pays qu'aucune autre donnée n'utilise (RG-REF-04)")
    void supprimer(Connecte connecte, @PathVariable long id) {
        gerer.supprimer(connecte.utilisateur(), id);
    }
}
