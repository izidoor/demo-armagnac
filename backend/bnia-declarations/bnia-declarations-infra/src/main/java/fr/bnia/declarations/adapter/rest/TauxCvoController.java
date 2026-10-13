package fr.bnia.declarations.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.declarations.adapter.rest.CotisationResponse.TauxCvoResponse;
import fr.bnia.declarations.application.GererGrilleCotisation;
import fr.bnia.declarations.application.GererGrilleCotisation.SaisieTauxCvo;
import fr.bnia.declarations.domain.GrilleCotisation.TauxEnVigueur;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * La grille des taux CVO, menu Paramétrage (spec 05 §3.5, §6.1). Chaque écriture renvoie toute la chaîne,
 * recomposée (RG-DEC-30) ; un taux est désigné par sa date de début.
 */
@RestController
@RequestMapping("/api/declarations/taux-cvo")
@Tag(name = "declarations")
class TauxCvoController {

    @Schema(name = "SaisieTauxCvo", description = "Saisie d'un taux CVO (RG-DEC-30)")
    record SaisieTauxCvoRequest(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "€/hlAP, strictement positif", example = "60.00")
            BigDecimal montant,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "De 0 à 100", example = "20.00")
            BigDecimal pourcentageExonere,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-01-01") LocalDate debut) {

        SaisieTauxCvo saisie() {
            return new SaisieTauxCvo(montant, pourcentageExonere, debut);
        }
    }

    private final GererGrilleCotisation grille;

    TauxCvoController(GererGrilleCotisation grille) {
        this.grille = grille;
    }

    @GetMapping
    @Operation(operationId = "listerTauxCvo", summary = "La grille CVO, du plus ancien au plus récent, avec la fin calculée de chaque taux")
    List<TauxCvoResponse> lister(Connecte connecte) {
        return reponse(grille.consulter(Acteurs.de(connecte)));
    }

    @PostMapping
    @Operation(operationId = "ajouterTauxCvo", summary = "Ajouter un taux CVO ; il clôt le précédent la veille de son début (RG-DEC-30)")
    List<TauxCvoResponse> ajouter(Connecte connecte, @RequestBody SaisieTauxCvoRequest saisie) {
        return reponse(grille.ajouter(Acteurs.de(connecte), saisie.saisie()));
    }

    @PutMapping("/{debut}")
    @Operation(operationId = "modifierTauxCvo", summary = "Corriger le taux CVO qui commence à cette date (RG-DEC-30)")
    List<TauxCvoResponse> modifier(Connecte connecte, @PathVariable LocalDate debut,
            @RequestBody SaisieTauxCvoRequest saisie) {
        return reponse(grille.modifier(Acteurs.de(connecte), debut, saisie.saisie()));
    }

    @DeleteMapping("/{debut}")
    @Operation(operationId = "supprimerTauxCvo", summary = "Supprimer le taux CVO qui commence à cette date ; la chaîne se recompose (RG-DEC-30)")
    List<TauxCvoResponse> supprimer(Connecte connecte, @PathVariable LocalDate debut) {
        return reponse(grille.supprimer(Acteurs.de(connecte), debut));
    }

    private static List<TauxCvoResponse> reponse(List<TauxEnVigueur> chaine) {
        return chaine.stream().map(TauxCvoResponse::depuis).toList();
    }
}
