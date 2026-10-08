package fr.bnia.referentiels.adapter.rest;

import fr.bnia.referentiels.application.FiltreValeurs;
import fr.bnia.referentiels.application.GererContenances;
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
@RequestMapping("/api/referentiels/contenances")
@Tag(name = "contenances", description = "Référentiel : contenances (C.10.9.4)")
class ContenanceController {

    private final GererContenances gerer;

    ContenanceController(GererContenances gerer) {
        this.gerer = gerer;
    }

    @GetMapping
    @Operation(operationId = "listerContenances", summary = "Lister les contenances, par volume croissant")
    List<ContenanceResponse> lister(
            @Parameter(description = "Absent : actives et inactives") @RequestParam(required = false) Boolean actif,
            @Parameter(description = "Texte cherché dans le libellé saisi ou le volume") @RequestParam(required = false) String recherche) {
        return gerer.lister(new FiltreValeurs(actif, recherche)).stream().map(ContenanceResponse::depuis).toList();
    }

    @GetMapping("/{id}")
    @Operation(operationId = "consulterContenance", summary = "Consulter une contenance, active ou non")
    ContenanceResponse consulter(@PathVariable long id) {
        return ContenanceResponse.depuis(gerer.consulter(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "creerContenance", summary = "Créer une contenance (RG-REF-10)")
    ContenanceResponse creer(@RequestBody SaisieContenanceRequest requete) {
        return ContenanceResponse.depuis(gerer.creer(requete.volume(), requete.libelle()));
    }

    @PutMapping("/{id}")
    @Operation(operationId = "modifierContenance", summary = "Modifier le volume et le libellé (RG-REF-02, RG-REF-10)")
    ContenanceResponse modifier(@PathVariable long id, @RequestBody SaisieContenanceRequest requete) {
        return ContenanceResponse.depuis(gerer.modifier(id, requete.volume(), requete.libelle()));
    }

    @PostMapping("/{id}/desactivation")
    @Operation(operationId = "desactiverContenance", summary = "Désactiver une contenance (RG-REF-04)")
    ContenanceResponse desactiver(@PathVariable long id) {
        return ContenanceResponse.depuis(gerer.desactiver(id));
    }

    @PostMapping("/{id}/reactivation")
    @Operation(operationId = "reactiverContenance", summary = "Réactiver une contenance (RG-REF-04)")
    ContenanceResponse reactiver(@PathVariable long id) {
        return ContenanceResponse.depuis(gerer.reactiver(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "supprimerContenance", summary = "Supprimer une contenance qu'aucune autre donnée n'utilise (RG-REF-04)")
    void supprimer(@PathVariable long id) {
        gerer.supprimer(id);
    }
}
