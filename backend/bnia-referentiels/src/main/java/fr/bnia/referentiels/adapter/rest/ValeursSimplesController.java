package fr.bnia.referentiels.adapter.rest;

import fr.bnia.referentiels.application.FiltreValeurs;
import fr.bnia.referentiels.application.GererValeursSimples;
import fr.bnia.referentiels.domain.ValeurSimple;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Endpoints communs aux listes simples. Chaque liste a son controller (A-REF-3), qui fixe le chemin,
 * le tag et la {@link fr.bnia.commun.adapter.rest.Ressource} nommant ses opérations. Les valeurs sont
 * désignées par leur identifiant technique (ADR-011).
 */
abstract class ValeursSimplesController<T extends ValeurSimple> {

    private final GererValeursSimples<T> gerer;

    protected ValeursSimplesController(GererValeursSimples<T> gerer) {
        this.gerer = gerer;
    }

    @GetMapping
    @Operation(summary = "Lister les valeurs, triées par libellé")
    List<ValeurSimpleResponse> lister(
            @Parameter(description = "Absent : actives et inactives") @RequestParam(required = false) Boolean actif,
            @Parameter(description = "Texte cherché dans le code ou le libellé") @RequestParam(required = false) String recherche) {
        return gerer.lister(new FiltreValeurs(actif, recherche)).stream()
                .map(ValeurSimpleResponse::depuis)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une valeur, active ou non")
    ValeurSimpleResponse consulter(@PathVariable long id) {
        return ValeurSimpleResponse.depuis(gerer.consulter(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une valeur (RG-REF-01, RG-REF-03)")
    ValeurSimpleResponse creer(@RequestBody SaisieValeurSimpleRequest requete) {
        return ValeurSimpleResponse.depuis(gerer.creer(requete.code(), requete.libelle()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier le code et le libellé (RG-REF-01 à 03)")
    ValeurSimpleResponse modifier(@PathVariable long id, @RequestBody SaisieValeurSimpleRequest requete) {
        return ValeurSimpleResponse.depuis(gerer.modifier(id, requete.code(), requete.libelle()));
    }

    @PostMapping("/{id}/desactivation")
    @Operation(summary = "Désactiver une valeur (RG-REF-04)")
    ValeurSimpleResponse desactiver(@PathVariable long id) {
        return ValeurSimpleResponse.depuis(gerer.desactiver(id));
    }

    @PostMapping("/{id}/reactivation")
    @Operation(summary = "Réactiver une valeur (RG-REF-04)")
    ValeurSimpleResponse reactiver(@PathVariable long id) {
        return ValeurSimpleResponse.depuis(gerer.reactiver(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une valeur qu'aucune autre donnée n'utilise ; sinon 409, la désactiver (RG-REF-04)")
    void supprimer(@PathVariable long id) {
        gerer.supprimer(id);
    }
}
