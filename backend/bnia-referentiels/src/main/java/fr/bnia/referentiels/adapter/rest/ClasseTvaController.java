package fr.bnia.referentiels.adapter.rest;

import fr.bnia.referentiels.application.FiltreValeurs;
import fr.bnia.referentiels.application.GererClassesTva;
import fr.bnia.referentiels.domain.ClasseTva;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
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

/**
 * Classes de TVA et l'historique de leurs taux. Chaque opération sur un taux renvoie la classe
 * entière, avec son taux en vigueur.
 */
@RestController
@RequestMapping("/api/referentiels/classes-tva")
@Tag(name = "classes-tva", description = "Référentiel : classes et taux de TVA (C.10.1-2)")
class ClasseTvaController {

    private final GererClassesTva gerer;

    ClasseTvaController(GererClassesTva gerer) {
        this.gerer = gerer;
    }

    @GetMapping
    @Operation(operationId = "listerClassesTva", summary = "Lister les classes de TVA, avec leur taux en vigueur")
    List<ClasseTvaLigneResponse> lister(
            @Parameter(description = "Absent : actives et inactives") @RequestParam(required = false) Boolean actif,
            @Parameter(description = "Texte cherché dans le code ou le libellé") @RequestParam(required = false) String recherche) {
        return gerer.lister(new FiltreValeurs(actif, recherche)).stream().map(ClasseTvaLigneResponse::depuis).toList();
    }

    @GetMapping("/{code}")
    @Operation(operationId = "consulterClasseTva", summary = "Consulter une classe et ses taux, même inactive (RG-REF-13)")
    ClasseTvaResponse consulter(@PathVariable String code) {
        return reponse(gerer.consulter(code));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "creerClasseTva", summary = "Créer une classe de TVA, sans taux (RG-REF-01, RG-REF-03)")
    ClasseTvaResponse creer(@RequestBody CreationValeurSimpleRequest requete) {
        return reponse(gerer.creer(requete.code(), requete.libelle()));
    }

    @PutMapping("/{code}")
    @Operation(operationId = "modifierClasseTva", summary = "Modifier le libellé ; le code est immuable (RG-REF-02)")
    ClasseTvaResponse modifier(@PathVariable String code, @RequestBody ModificationValeurSimpleRequest requete) {
        return reponse(gerer.modifier(code, requete.libelle()));
    }

    @PostMapping("/{code}/desactivation")
    @Operation(operationId = "desactiverClasseTva", summary = "Désactiver une classe ; ses taux restent consultables (RG-REF-13)")
    ClasseTvaResponse desactiver(@PathVariable String code) {
        return reponse(gerer.desactiver(code));
    }

    @PostMapping("/{code}/reactivation")
    @Operation(operationId = "reactiverClasseTva", summary = "Réactiver une classe (RG-REF-04)")
    ClasseTvaResponse reactiver(@PathVariable String code) {
        return reponse(gerer.reactiver(code));
    }

    @PostMapping("/{code}/taux")
    @Operation(operationId = "ajouterTauxTva", summary = "Ajouter un taux, sans chevauchement (RG-REF-11)")
    ClasseTvaResponse ajouterTaux(@PathVariable String code, @RequestBody SaisieTauxTvaRequest requete) {
        return reponse(gerer.ajouterTaux(code, requete.versSaisie()));
    }

    @PutMapping("/{code}/taux/{dateDebut}")
    @Operation(operationId = "modifierTauxTva", summary = "Corriger un taux, désigné par sa date de début (RG-REF-11)")
    ClasseTvaResponse modifierTaux(@PathVariable String code, @PathVariable LocalDate dateDebut,
            @RequestBody SaisieTauxTvaRequest requete) {
        return reponse(gerer.modifierTaux(code, dateDebut, requete.versSaisie()));
    }

    @DeleteMapping("/{code}/taux/{dateDebut}")
    @Operation(operationId = "retirerTauxTva", summary = "Retirer un taux saisi par erreur")
    ClasseTvaResponse retirerTaux(@PathVariable String code, @PathVariable LocalDate dateDebut) {
        return reponse(gerer.retirerTaux(code, dateDebut));
    }

    private ClasseTvaResponse reponse(ClasseTva classe) {
        return ClasseTvaResponse.depuis(classe, gerer.aujourdhui());
    }
}
