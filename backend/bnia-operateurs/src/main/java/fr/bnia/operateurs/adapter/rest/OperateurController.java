package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.FiltreOperateurs;
import fr.bnia.operateurs.application.GererContacts;
import fr.bnia.operateurs.application.GererOperateurs;
import fr.bnia.operateurs.domain.Canal;
import fr.bnia.operateurs.domain.CodeOperateur;
import fr.bnia.operateurs.domain.Operateur;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
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

/**
 * Opérateurs au back-office (spec 02, §5.1). Chaque écriture renvoie la fiche entière, avec ce qu'elle
 * signale.
 */
@RestController
@RequestMapping("/api/operateurs")
@Tag(name = "operateurs", description = "Opérateurs : fiche, identité, code (§4.3, C.1.1)")
class OperateurController {

    private final GererOperateurs gerer;
    private final GererContacts contacts;

    OperateurController(GererOperateurs gerer, GererContacts contacts) {
        this.gerer = gerer;
        this.contacts = contacts;
    }

    @GetMapping
    @Operation(operationId = "listerOperateurs", summary = "Lister les opérateurs, triés par raison sociale (§4.3.1)")
    List<OperateurLigneResponse> lister(
            @RequestParam(required = false) TypeOperateur type,
            @Parameter(description = "Contient") @RequestParam(required = false) String code,
            @Parameter(description = "Contient") @RequestParam(required = false) String raisonSociale,
            @Parameter(description = "Contient") @RequestParam(required = false) String nomCommercial,
            @Parameter(description = "Commence par") @RequestParam(required = false) String codePostal,
            @Parameter(description = "Contient") @RequestParam(required = false) String ville,
            @Parameter(description = "Absent : actifs et inactifs") @RequestParam(required = false) Boolean actif,
            @Parameter(description = "Vrai : fiches incomplètes seulement (RG-OPE-02)") @RequestParam(required = false) Boolean incomplet) {
        var filtre = new FiltreOperateurs(type, code, raisonSociale, nomCommercial, codePostal, ville, actif, incomplet);
        return gerer.lister(filtre).stream().map(OperateurLigneResponse::depuis).toList();
    }

    @GetMapping("/{id}")
    @Operation(operationId = "consulterOperateur", summary = "Consulter la fiche d'un opérateur, actif ou non")
    FicheOperateurResponse consulter(@PathVariable long id) {
        return fiche(id);
    }

    @GetMapping("/code-propose")
    @Operation(operationId = "proposerCodeOperateur", summary = "Code proposé pour un type et un code postal (RG-OPE-03)")
    CodeProposeResponse proposerCode(
            @RequestParam TypeOperateur type,
            @RequestParam(required = false) String codePostal,
            @Parameter(description = "Identifiant du pays ; absent : la France") @RequestParam(required = false) Long pays) {
        return new CodeProposeResponse(gerer.proposerCode(type, codePostal, pays).map(CodeOperateur::valeur).orElse(null));
    }

    @GetMapping("/doublons")
    @Operation(operationId = "listerDoublonsOperateur",
            summary = "Fiches qui portent déjà la raison sociale, le CVI, le SIRET ou le n° d'accise saisis (RG-OPE-06, 08)")
    List<DoublonResponse> doublons(
            @RequestParam(required = false) String raisonSociale,
            @RequestParam(required = false) String cvi,
            @RequestParam(required = false) String siret,
            @RequestParam(required = false) String accise,
            @Parameter(description = "Opérateur en cours de modification, à exclure") @RequestParam(required = false) Long sauf) {
        return gerer.doublons(raisonSociale, cvi, siret, accise, sauf).stream().map(DoublonResponse::depuis).toList();
    }

    @GetMapping("/groupes")
    @Operation(operationId = "listerGroupesOperateurs", summary = "Groupes existants, proposés à la saisie (RG-OPE-09)")
    List<String> groupes(@RequestParam(required = false) String recherche) {
        return gerer.groupes(recherche);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "creerOperateur", summary = "Créer un opérateur ; sans code saisi, le code proposé (RG-OPE-01 à 08, 20, 21, 24)")
    FicheOperateurResponse creer(@RequestBody SaisieOperateurRequest requete) {
        return reponse(gerer.creer(requete.versSaisie()));
    }

    @PutMapping("/{id}/identite")
    @Operation(operationId = "modifierIdentiteOperateur",
            summary = "Modifier l'identité, type et code compris ; le code ne change pas seul (RG-OPE-01 à 09, 20, 21, 24)")
    FicheOperateurResponse modifierIdentite(@PathVariable long id, @RequestBody SaisieOperateurRequest requete) {
        return reponse(gerer.modifierIdentite(id, requete.versSaisie()));
    }

    @PutMapping("/{id}/production")
    @Operation(operationId = "modifierProductionOperateur",
            summary = "Modifier le déclaratif, la région, les appellations et les alambics (RG-OPE-01, 10 à 13, 21)")
    FicheOperateurResponse modifierProduction(@PathVariable long id, @RequestBody SaisieProductionRequest requete) {
        return reponse(gerer.modifierProduction(id, requete.versSaisie()));
    }

    @PutMapping("/{id}/diffusion")
    @Operation(operationId = "modifierDiffusionOperateur",
            summary = "Modifier la diffusion des millésimes et l'accès aux statistiques du portail (RG-OPE-14, 15)")
    FicheOperateurResponse modifierDiffusion(@PathVariable long id, @RequestBody SaisieDiffusionRequest requete) {
        return reponse(gerer.modifierDiffusion(id, requete.versSaisie()));
    }

    @PutMapping("/{id}/designations")
    @Operation(operationId = "designerContactsOperateur",
            summary = "Désigner le contact principal et le destinataire des factures (RG-OPE-22, 23, 24)")
    FicheOperateurResponse designer(@PathVariable long id, @RequestBody SaisieDesignationsRequest requete) {
        return reponse(contacts.designer(id, requete.versSaisie(), Canal.BACK_OFFICE));
    }

    @PostMapping("/{id}/nouveau-code")
    @Operation(operationId = "attribuerNouveauCodeOperateur", summary = "Attribuer le code proposé pour le type et le code postal actuels (RG-OPE-05)")
    FicheOperateurResponse attribuerNouveauCode(@PathVariable long id) {
        return reponse(gerer.attribuerNouveauCode(id));
    }

    @PostMapping("/{id}/desactivation")
    @Operation(operationId = "desactiverOperateur",
            summary = "Désactiver un opérateur : consultable, plus proposé ; ses contacts perdent l'accès au portail (RG-OPE-26)")
    FicheOperateurResponse desactiver(@PathVariable long id) {
        return reponse(gerer.desactiver(id));
    }

    @PostMapping("/{id}/reactivation")
    @Operation(operationId = "reactiverOperateur", summary = "Réactiver un opérateur (RG-OPE-26)")
    FicheOperateurResponse reactiver(@PathVariable long id) {
        return reponse(gerer.reactiver(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "supprimerOperateur", summary = "Supprimer un opérateur que rien ne référence (RG-OPE-25)")
    void supprimer(@PathVariable long id) {
        gerer.supprimer(id);
    }

    private FicheOperateurResponse reponse(Operateur operateur) {
        return fiche(operateur.id());
    }

    private FicheOperateurResponse fiche(long id) {
        return FicheOperateurResponse.depuis(gerer.fiche(id));
    }
}
