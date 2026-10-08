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

    @GetMapping("/{id}")
    @Operation(operationId = "consulterClasseTva", summary = "Consulter une classe et ses taux, même inactive (RG-REF-13)")
    ClasseTvaResponse consulter(@PathVariable long id) {
        return reponse(gerer.consulter(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "creerClasseTva", summary = "Créer une classe de TVA, sans taux (RG-REF-01, RG-REF-03)")
    ClasseTvaResponse creer(@RequestBody SaisieValeurSimpleRequest requete) {
        return reponse(gerer.creer(requete.code(), requete.libelle()));
    }

    @PutMapping("/{id}")
    @Operation(operationId = "modifierClasseTva", summary = "Modifier le code et le libellé (RG-REF-01 à 03)")
    ClasseTvaResponse modifier(@PathVariable long id, @RequestBody SaisieValeurSimpleRequest requete) {
        return reponse(gerer.modifier(id, requete.code(), requete.libelle()));
    }

    @PostMapping("/{id}/desactivation")
    @Operation(operationId = "desactiverClasseTva", summary = "Désactiver une classe ; ses taux restent consultables (RG-REF-13)")
    ClasseTvaResponse desactiver(@PathVariable long id) {
        return reponse(gerer.desactiver(id));
    }

    @PostMapping("/{id}/reactivation")
    @Operation(operationId = "reactiverClasseTva", summary = "Réactiver une classe (RG-REF-04)")
    ClasseTvaResponse reactiver(@PathVariable long id) {
        return reponse(gerer.reactiver(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "supprimerClasseTva", summary = "Supprimer une classe, avec ses taux, qu'aucune autre donnée n'utilise (RG-REF-04)")
    void supprimer(@PathVariable long id) {
        gerer.supprimer(id);
    }

    @PostMapping("/{id}/taux")
    @Operation(operationId = "ajouterTauxTva", summary = "Ajouter un taux, sans chevauchement (RG-REF-11)")
    ClasseTvaResponse ajouterTaux(@PathVariable long id, @RequestBody SaisieTauxTvaRequest requete) {
        return reponse(gerer.ajouterTaux(id, requete.versSaisie()));
    }

    @PutMapping("/{id}/taux/{dateDebut}")
    @Operation(operationId = "modifierTauxTva", summary = "Corriger un taux, désigné par sa date de début (RG-REF-11)")
    ClasseTvaResponse modifierTaux(@PathVariable long id, @PathVariable LocalDate dateDebut,
            @RequestBody SaisieTauxTvaRequest requete) {
        return reponse(gerer.modifierTaux(id, dateDebut, requete.versSaisie()));
    }

    @DeleteMapping("/{id}/taux/{dateDebut}")
    @Operation(operationId = "retirerTauxTva", summary = "Retirer un taux saisi par erreur")
    ClasseTvaResponse retirerTaux(@PathVariable long id, @PathVariable LocalDate dateDebut) {
        return reponse(gerer.retirerTaux(id, dateDebut));
    }

    private ClasseTvaResponse reponse(ClasseTva classe) {
        return ClasseTvaResponse.depuis(classe, gerer.aujourdhui());
    }
}
