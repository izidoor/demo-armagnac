package fr.bnia.declarations.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.declarations.application.GererDsm;
import fr.bnia.declarations.domain.partage.Cote;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * DSM d'un opérateur (analyse UC-DEC-4, spec 05 §6.2, §6.3), module Déclarations. Mêmes endpoints au
 * back-office et au portail : les droits font la différence (RG-DEC-01).
 */
@RestController
@RequestMapping("/api/operateurs/{operateur}")
@Tag(name = "declarations", description = "Déclarations de stocks et mouvements (spec 05)")
class DsmController {

    private final GererDsm gerer;

    DsmController(GererDsm gerer) {
        this.gerer = gerer;
    }

    @GetMapping("/dsm")
    @Operation(operationId = "listerDsm", summary = "Les DSM de l'opérateur, de la plus récente à la plus ancienne (RG-DEC-01)")
    List<DsmResumeResponse> lister(Connecte connecte, @PathVariable long operateur) {
        Cote cote = gerer.cote(Acteurs.de(connecte), operateur);
        return gerer.lister(Acteurs.de(connecte), operateur).stream().map(d -> DsmResumeResponse.depuis(d, cote))
                .toList();
    }

    @GetMapping("/dsm/situation")
    @Operation(operationId = "consulterSituationDsm",
            summary = "La prochaine DSM de l'opérateur, ou ce qui en empêche l'initiation (RG-DEC-02 à 05)")
    SituationDsmResponse situation(Connecte connecte, @PathVariable long operateur) {
        return SituationDsmResponse.depuis(gerer.situation(Acteurs.de(connecte), operateur));
    }

    @PostMapping("/dsm")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "initierDsm",
            summary = "L'opérateur initie la DSM de la période attendue, reprise du stock précédent (RG-DEC-01 à 05, 13, 14)")
    DsmResponse initier(Connecte connecte, @PathVariable long operateur) {
        return DsmResponse.depuis(gerer.initier(Acteurs.de(connecte), operateur));
    }

    @GetMapping("/dsm/{id}")
    @Operation(operationId = "consulterDsm", summary = "Une DSM, ses totaux et ses anomalies (RG-DEC-01, 16 à 18)")
    DsmResponse consulter(Connecte connecte, @PathVariable long operateur, @PathVariable long id) {
        return DsmResponse.depuis(gerer.consulter(Acteurs.de(connecte), operateur, id));
    }

    @PutMapping("/dsm/{id}")
    @Operation(operationId = "enregistrerDsm",
            summary = "L'opérateur enregistre sa saisie ; les anomalies ne l'empêchent pas (RG-DEC-10 à 13, 15 à 19, 21)")
    DsmResponse enregistrer(Connecte connecte, @PathVariable long operateur, @PathVariable long id,
            @RequestBody SaisieDsmRequest requete) {
        return DsmResponse.depuis(gerer.enregistrer(Acteurs.de(connecte), operateur, id, requete.versSaisie(),
                requete.version()));
    }

    @PostMapping("/dsm/{id}/apercu")
    @Operation(operationId = "apercevoirDsm",
            summary = "Vue provisoire d'une saisie non enregistrée : totaux, comptes MB, anomalies ; rien n'est enregistré")
    DsmResponse apercevoir(Connecte connecte, @PathVariable long operateur, @PathVariable long id,
            @RequestBody SaisieDsmRequest requete) {
        return DsmResponse.depuis(gerer.apercevoir(Acteurs.de(connecte), operateur, id, requete.versSaisie()));
    }

    @PostMapping("/dsm/{id}/envoi")
    @Operation(operationId = "envoyerDsm",
            summary = "L'opérateur envoie sa DSM sans anomalie, ou la renvoie après compléments (RG-DEC-18, 20)")
    DsmResponse envoyer(Connecte connecte, @PathVariable long operateur, @PathVariable long id,
            @RequestBody TransitionDsmRequest requete) {
        return DsmResponse.depuis(gerer.envoyer(Acteurs.de(connecte), operateur, id, requete.version()));
    }

    @PostMapping("/dsm/{id}/prise-en-instruction")
    @Operation(operationId = "prendreDsmEnInstruction", summary = "Le BNIA prend en instruction une DSM envoyée (RG-DEC-20)")
    DsmResponse prendreEnInstruction(Connecte connecte, @PathVariable long operateur, @PathVariable long id,
            @RequestBody TransitionDsmRequest requete) {
        return DsmResponse.depuis(gerer.prendreEnInstruction(Acteurs.de(connecte), operateur, id, requete.version()));
    }

    @PostMapping("/dsm/{id}/complements")
    @Operation(operationId = "demanderComplementsDsm",
            summary = "Le BNIA demande des compléments, avec un motif écrit en commentaire de la DSM (RG-DEC-20, 24)")
    DsmResponse demanderComplements(Connecte connecte, @PathVariable long operateur, @PathVariable long id,
            @RequestBody ComplementsDsmRequest requete) {
        return DsmResponse.depuis(gerer.demanderComplements(Acteurs.de(connecte), operateur, id, requete.motif(),
                requete.version()));
    }

    @PostMapping("/dsm/{id}/validation")
    @Operation(operationId = "validerDsm", summary = "Le BNIA valide une DSM en instruction ; elle est figée (RG-DEC-20)")
    DsmResponse valider(Connecte connecte, @PathVariable long operateur, @PathVariable long id,
            @RequestBody TransitionDsmRequest requete) {
        return DsmResponse.depuis(gerer.valider(Acteurs.de(connecte), operateur, id, requete.version()));
    }

    @DeleteMapping("/dsm/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "supprimerDsm",
            summary = "Supprimer un brouillon, par l'opérateur ou le BNIA ; commentaires et journal avec lui (RG-DEC-01, 22)")
    void supprimer(Connecte connecte, @PathVariable long operateur, @PathVariable long id) {
        gerer.supprimer(Acteurs.de(connecte), operateur, id);
    }

    @GetMapping("/dsm/{id}/journal")
    @Operation(operationId = "consulterJournalDsm", summary = "Historique d'une DSM, du plus récent au plus ancien (RG-DEC-23)")
    List<EntreeJournalResponse> journal(Connecte connecte, @PathVariable long operateur, @PathVariable long id) {
        return gerer.journal(Acteurs.de(connecte), operateur, id).stream().map(EntreeJournalResponse::depuis).toList();
    }

    @GetMapping("/declarations/journal")
    @Operation(operationId = "consulterJournalDeclarations",
            summary = "Journal des déclarations de l'opérateur, brouillons supprimés compris (RG-DEC-23)")
    List<EntreeJournalResponse> journalDeLOperateur(Connecte connecte, @PathVariable long operateur) {
        return gerer.journalDeLOperateur(Acteurs.de(connecte), operateur).stream().map(EntreeJournalResponse::depuis)
                .toList();
    }

    @GetMapping("/declarations/denominations")
    @Operation(operationId = "listerDenominations",
            summary = "Dénominations déjà employées par l'opérateur, suggérées à la saisie (H-DEC-8)")
    List<String> denominations(Connecte connecte, @PathVariable long operateur) {
        return gerer.denominations(Acteurs.de(connecte), operateur);
    }
}
