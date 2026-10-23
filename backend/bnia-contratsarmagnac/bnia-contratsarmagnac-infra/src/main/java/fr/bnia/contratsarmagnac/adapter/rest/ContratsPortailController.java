package fr.bnia.contratsarmagnac.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.contratsarmagnac.application.ConsulterContrats;
import fr.bnia.contratsarmagnac.application.EditerContrats;
import fr.bnia.contratsarmagnac.application.ExecuterContrats;
import fr.bnia.contratsarmagnac.application.NegocierContrats.SaisieContrat;
import fr.bnia.contratsarmagnac.application.NegocierContrats;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrats au portail (spec 06 §6.2) : ceux de l'opérateur actif, partie au contrat ; négociation et conclusion
 * (UC-CA-1, 2), retiraisons, annulation et clôture (UC-CA-3 à 5). Les droits se vérifient dans les cas d'usage
 * (RG-CA-01).
 */
@RestController
@RequestMapping("/api/operateurs/{operateur}/contrats")
@Tag(name = "contratsarmagnac", description = "Contrats d'achat Armagnac (spec 06)")
class ContratsPortailController {

    private final NegocierContrats negocier;
    private final ExecuterContrats executer;
    private final ConsulterContrats consulter;
    private final EditerContrats editer;

    ContratsPortailController(NegocierContrats negocier, ExecuterContrats executer, ConsulterContrats consulter,
            EditerContrats editer) {
        this.negocier = negocier;
        this.executer = executer;
        this.consulter = consulter;
        this.editer = editer;
    }

    @GetMapping
    @Operation(operationId = "listerMesContrats",
            summary = "Les contrats de l'opérateur actif, chacun rangé dans son onglet (RG-CA-01, 25)")
    List<ContratListeResponse> lister(Connecte connecte, @PathVariable long operateur) {
        return consulter.duPortail(Acteurs.de(connecte), operateur).stream().map(ContratListeResponse::depuis).toList();
    }

    @GetMapping("/stock")
    @Operation(operationId = "consulterStockPourContrat",
            summary = "Le stock estimé sous bois de l'opérateur actif, où le vendeur choisit ses lignes (RG-CA-11, 12)")
    List<LigneStockContratResponse> stock(Connecte connecte, @PathVariable long operateur) {
        return consulter.stockSousBois(Acteurs.de(connecte), operateur).stream()
                .map(LigneStockContratResponse::depuis).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "ouvrirContrat",
            summary = "L'opérateur actif ouvre un projet ; le vendeur, dans la limite de son stock estimé (RG-CA-01 à 06, 12, 18, 19)")
    ContratResponse ouvrir(Connecte connecte, @PathVariable long operateur, @RequestBody SaisieContratRequest requete) {
        long id = negocier.ouvrir(Acteurs.de(connecte), operateur, new SaisieContrat(requete.partie(),
                requete.autrePartie(), requete.contenu().versSaisie())).id();
        return consulter(connecte, operateur, id);
    }

    @GetMapping("/{id}")
    @Operation(operationId = "consulterMonContrat",
            summary = "Un contrat de l'opérateur actif, avec l'écart à approuver et le journal (RG-CA-01, 09, 19)")
    ContratResponse consulter(Connecte connecte, @PathVariable long operateur, @PathVariable long id) {
        return ContratResponse.depuis(consulter.auPortail(Acteurs.de(connecte), operateur, id));
    }

    @PutMapping("/{id}")
    @Operation(operationId = "modifierContrat",
            summary = "Une nouvelle version du projet, approuvée par son auteur seul (RG-CA-07, 12, 18, 19)")
    ContratResponse modifier(Connecte connecte, @PathVariable long operateur, @PathVariable long id,
            @RequestBody ModificationContratRequest requete) {
        negocier.modifier(Acteurs.de(connecte), operateur, id, requete.versionVue(), requete.contenu().versSaisie());
        return consulter(connecte, operateur, id);
    }

    @PostMapping("/{id}/approbation")
    @Operation(operationId = "approuverContrat",
            summary = "La partie attendue approuve la version courante : le contrat est conclu (RG-CA-08, 12, 18, 19)")
    ContratResponse approuver(Connecte connecte, @PathVariable long operateur, @PathVariable long id,
            @RequestBody DecisionContratRequest requete) {
        negocier.approuver(Acteurs.de(connecte), operateur, id, requete.versionVue());
        return consulter(connecte, operateur, id);
    }

    @PostMapping("/{id}/refus")
    @Operation(operationId = "refuserContrat", summary = "La partie attendue refuse le projet, définitivement (RG-CA-10, 18, 19)")
    ContratResponse refuser(Connecte connecte, @PathVariable long operateur, @PathVariable long id,
            @RequestBody DecisionContratRequest requete) {
        negocier.refuser(Acteurs.de(connecte), operateur, id, requete.versionVue());
        return consulter(connecte, operateur, id);
    }

    @PostMapping("/{id}/retiraisons")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "declarerRetiraison",
            summary = "L'acheteur déclare une retiraison, que le stock estimé du vendeur doit couvrir (RG-CA-13, 18, 19)")
    ContratResponse declarerRetiraison(Connecte connecte, @PathVariable long operateur, @PathVariable long id,
            @RequestBody SaisieRetiraisonRequest requete) {
        executer.declarerRetiraison(Acteurs.de(connecte), operateur, id, requete.versSaisie());
        return consulter(connecte, operateur, id);
    }

    @PutMapping("/{id}/retiraisons/{numero}")
    @Operation(operationId = "modifierRetiraison",
            summary = "La partie qui doit valider la retiraison la modifie ; elle attend alors l'autre (RG-CA-14, 18, 19)")
    ContratResponse modifierRetiraison(Connecte connecte, @PathVariable long operateur, @PathVariable long id,
            @PathVariable int numero, @RequestBody ModificationRetiraisonRequest requete) {
        executer.modifierRetiraison(Acteurs.de(connecte), operateur, id, numero, requete.versionVue(),
                requete.contenu().versSaisie());
        return consulter(connecte, operateur, id);
    }

    @PostMapping("/{id}/retiraisons/{numero}/validation")
    @Operation(operationId = "validerRetiraison",
            summary = "La partie attendue valide la retiraison : stocks estimés et DSM la reçoivent (RG-CA-15, 18, 19, 21, 22)")
    ContratResponse validerRetiraison(Connecte connecte, @PathVariable long operateur, @PathVariable long id,
            @PathVariable int numero, @RequestBody DecisionContratRequest requete) {
        executer.validerRetiraison(Acteurs.de(connecte), operateur, id, numero, requete.versionVue());
        return consulter(connecte, operateur, id);
    }

    @DeleteMapping("/{id}/retiraisons/{numero}")
    @Operation(operationId = "retirerRetiraison", summary = "L'acheteur retire une retiraison non validée (RG-CA-16, 19)")
    ContratResponse retirerRetiraison(Connecte connecte, @PathVariable long operateur, @PathVariable long id,
            @PathVariable int numero) {
        executer.retirerRetiraison(Acteurs.de(connecte), operateur, id, numero);
        return consulter(connecte, operateur, id);
    }

    @PostMapping("/{id}/annulation")
    @Operation(operationId = "annulerContrat",
            summary = "Une partie annule le contrat conclu ; les retiraisons validées restent (RG-CA-17, 18, 19)")
    ContratResponse annuler(Connecte connecte, @PathVariable long operateur, @PathVariable long id) {
        executer.annuler(Acteurs.de(connecte), operateur, id);
        return consulter(connecte, operateur, id);
    }

    @PostMapping("/{id}/cloture")
    @Operation(operationId = "cloturerContrat",
            summary = "Une partie déclare que le contrat conclu n'aura plus de retiraison : il est exécuté (RG-CA-17, 18, 19)")
    ContratResponse cloturer(Connecte connecte, @PathVariable long operateur, @PathVariable long id) {
        executer.cloturer(Acteurs.de(connecte), operateur, id);
        return consulter(connecte, operateur, id);
    }

    @GetMapping(path = "/{id}/pdf", produces = "application/pdf")
    @Operation(operationId = "editerMonContratPdf",
            summary = "Le PDF du contrat, au formulaire CA-05 allégé ; « Projet, non conclu » avant la conclusion ; inscrit au journal (RG-CA-19, 20)")
    ResponseEntity<byte[]> pdf(Connecte connecte, @PathVariable long operateur, @PathVariable long id) {
        return Pdf.reponse(editer.auPortail(Acteurs.de(connecte), operateur, id));
    }
}
