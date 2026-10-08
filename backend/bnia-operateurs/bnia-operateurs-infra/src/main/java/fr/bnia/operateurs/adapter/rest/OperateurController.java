package fr.bnia.operateurs.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.operateurs.application.ConsulterAnnuaire;
import fr.bnia.operateurs.application.FiltreOperateurs;
import fr.bnia.operateurs.application.GererOperateurs;
import fr.bnia.operateurs.domain.CodeOperateur;
import fr.bnia.operateurs.domain.Operateur;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
 * Opérateurs (spec 02, §5.1), mêmes endpoints au back-office et au portail : chaque cas d'usage vérifie
 * les droits de l'utilisateur connecté (ADR-016). Chaque écriture renvoie la fiche entière, avec ce
 * qu'elle signale et les droits du lecteur.
 */
@RestController
@RequestMapping("/api/operateurs")
@Tag(name = "operateurs", description = "Opérateurs : fiche, identité, code, annuaire, journal (§4.3, C.1.1)")
class OperateurController {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final GererOperateurs gerer;
    private final ConsulterAnnuaire annuaire;
    private final Clock horloge;

    OperateurController(GererOperateurs gerer, ConsulterAnnuaire annuaire, Clock horloge) {
        this.gerer = gerer;
        this.annuaire = annuaire;
        this.horloge = horloge;
    }

    @GetMapping
    @Operation(operationId = "listerOperateurs",
            summary = "Lister les opérateurs, sauf le BNIA, triés par raison sociale (§4.3.1, RG-OPE-32, 39)")
    List<OperateurLigneResponse> lister(Connecte connecte,
            @RequestParam(required = false) TypeOperateur type,
            @Parameter(description = "Contient") @RequestParam(required = false) String code,
            @Parameter(description = "Contient") @RequestParam(required = false) String raisonSociale,
            @Parameter(description = "Contient") @RequestParam(required = false) String nomCommercial,
            @Parameter(description = "Commence par") @RequestParam(required = false) String codePostal,
            @Parameter(description = "Contient") @RequestParam(required = false) String ville,
            @Parameter(description = "Absent : actifs et inactifs") @RequestParam(required = false) Boolean actif,
            @Parameter(description = "Vrai : fiches incomplètes seulement (RG-OPE-02)") @RequestParam(required = false) Boolean incomplet) {
        var filtre = new FiltreOperateurs(type, code, raisonSociale, nomCommercial, codePostal, ville, actif, incomplet);
        return gerer.lister(Acteurs.de(connecte), filtre).stream().map(OperateurLigneResponse::depuis).toList();
    }

    @GetMapping(value = "/export.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(operationId = "exporterOperateurs", summary = "Exporter le listing filtré au format Excel (RG-OPE-38)",
            responses = @ApiResponse(responseCode = "200", content = @Content(
                    mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    schema = @Schema(type = "string", format = "binary"))))
    ResponseEntity<byte[]> exporter(Connecte connecte,
            @RequestParam(required = false) TypeOperateur type,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String raisonSociale,
            @RequestParam(required = false) String nomCommercial,
            @RequestParam(required = false) String codePostal,
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) Boolean actif,
            @RequestParam(required = false) Boolean incomplet) {
        var filtre = new FiltreOperateurs(type, code, raisonSociale, nomCommercial, codePostal, ville, actif, incomplet);
        String fichier = "operateurs-" + LocalDate.now(horloge) + ".xlsx";
        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(fichier).build().toString())
                .body(ExportExcelOperateurs.classeur(gerer.lister(Acteurs.de(connecte), filtre)));
    }

    @GetMapping("/{id}")
    @Operation(operationId = "consulterOperateur", summary = "Consulter la fiche d'un opérateur, actif ou non (RG-OPE-31, 36)")
    FicheOperateurResponse consulter(Connecte connecte, @PathVariable long id) {
        return fiche(connecte, id);
    }

    @GetMapping("/{id}/mes-droits")
    @Operation(operationId = "consulterMesDroits", summary = "Droits de l'utilisateur connecté sur l'opérateur (RG-OPE-31, 32)")
    DroitsResponse mesDroits(Connecte connecte, @PathVariable long id) {
        return DroitsResponse.depuis(annuaire.mesDroits(Acteurs.de(connecte), id));
    }

    @GetMapping("/{id}/annuaire")
    @Operation(operationId = "consulterAnnuaire",
            summary = "Contacts, membres et invitations en attente de l'opérateur, en une liste (RG-OPE-52)")
    List<PersonneAnnuaireResponse> consulterAnnuaire(Connecte connecte, @PathVariable long id) {
        return annuaire.annuaire(Acteurs.de(connecte), id).stream().map(PersonneAnnuaireResponse::depuis).toList();
    }

    @GetMapping("/{id}/journal")
    @Operation(operationId = "consulterJournalOperateur",
            summary = "Actions sur les personnes et les accès de l'opérateur, du plus récent au plus ancien (RG-OPE-34)")
    List<LigneJournalResponse> consulterJournal(Connecte connecte, @PathVariable long id) {
        return annuaire.journal(Acteurs.de(connecte), id).stream().map(LigneJournalResponse::depuis).toList();
    }

    @GetMapping("/recherche")
    @Operation(operationId = "rechercherOperateurs",
            summary = "Rechercher un autre opérateur : actifs, trois caractères au moins, vingt résultats au plus (RG-OPE-37)")
    List<ResultatRechercheResponse> rechercher(Connecte connecte, @RequestParam String texte) {
        return gerer.rechercher(Acteurs.de(connecte), texte).stream().map(ResultatRechercheResponse::depuis).toList();
    }

    @GetMapping("/code-propose")
    @Operation(operationId = "proposerCodeOperateur", summary = "Code proposé pour un type et un code postal (RG-OPE-03)")
    CodeProposeResponse proposerCode(Connecte connecte,
            @RequestParam TypeOperateur type,
            @RequestParam(required = false) String codePostal,
            @Parameter(description = "Identifiant du pays ; absent : la France") @RequestParam(required = false) Long pays) {
        return new CodeProposeResponse(gerer.proposerCode(Acteurs.de(connecte), type, codePostal, pays)
                .map(CodeOperateur::valeur).orElse(null));
    }

    @GetMapping("/doublons")
    @Operation(operationId = "listerDoublonsOperateur",
            summary = "Fiches qui portent déjà la raison sociale, le CVI, le SIRET ou le n° d'accise saisis (RG-OPE-06, 08)")
    List<DoublonResponse> doublons(Connecte connecte,
            @RequestParam(required = false) String raisonSociale,
            @RequestParam(required = false) String cvi,
            @RequestParam(required = false) String siret,
            @RequestParam(required = false) String accise,
            @Parameter(description = "Opérateur en cours de modification, à exclure") @RequestParam(required = false) Long sauf) {
        return gerer.doublons(Acteurs.de(connecte), raisonSociale, cvi, siret, accise, sauf).stream()
                .map(DoublonResponse::depuis).toList();
    }

    @GetMapping("/groupes")
    @Operation(operationId = "listerGroupesOperateurs", summary = "Groupes existants, proposés à la saisie (RG-OPE-09)")
    List<String> groupes(Connecte connecte, @RequestParam(required = false) String recherche) {
        return gerer.groupes(Acteurs.de(connecte), recherche);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "creerOperateur",
            summary = "Créer un opérateur ; sans code saisi, le code proposé ; premier ADMIN invité au besoin (RG-OPE-01 à 08, 20, 21, 24, 39, 40)")
    FicheOperateurResponse creer(Connecte connecte, @RequestBody SaisieOperateurRequest requete) {
        return reponse(connecte, gerer.creer(Acteurs.de(connecte), requete.versSaisie(), requete.premierAdminSaisi()));
    }

    @PutMapping("/{id}/identite")
    @Operation(operationId = "modifierIdentiteOperateur",
            summary = "Modifier l'identité complète, réservé au BNIA ; le code ne change pas seul (RG-OPE-01 à 09, 20, 21, 24, 36, 51)")
    FicheOperateurResponse modifierIdentite(Connecte connecte, @PathVariable long id,
            @RequestBody SaisieOperateurRequest requete) {
        return reponse(connecte, gerer.modifierIdentite(Acteurs.de(connecte), id, requete.versSaisie()));
    }

    @PutMapping("/{id}/profil")
    @Operation(operationId = "modifierProfilOperateur",
            summary = "Modifier les champs qu'un opérateur modifie lui-même (RG-OPE-20, 21, 24, 36, 51)")
    FicheOperateurResponse modifierProfil(Connecte connecte, @PathVariable long id,
            @RequestBody SaisieProfilOperateurRequest requete) {
        return reponse(connecte, gerer.modifierProfil(Acteurs.de(connecte), id, requete.versSaisie()));
    }

    @PutMapping("/{id}/production")
    @Operation(operationId = "modifierProductionOperateur",
            summary = "Modifier le déclaratif, la région, les appellations et les alambics, réservé au BNIA (RG-OPE-01, 10 à 13, 21, 36)")
    FicheOperateurResponse modifierProduction(Connecte connecte, @PathVariable long id,
            @RequestBody SaisieProductionRequest requete) {
        return reponse(connecte, gerer.modifierProduction(Acteurs.de(connecte), id, requete.versSaisie()));
    }

    @PutMapping("/{id}/diffusion")
    @Operation(operationId = "modifierDiffusionOperateur",
            summary = "Modifier la diffusion des millésimes ; l'accès aux statistiques par le BNIA seul (RG-OPE-14, 15, 36)")
    FicheOperateurResponse modifierDiffusion(Connecte connecte, @PathVariable long id,
            @RequestBody SaisieDiffusionRequest requete) {
        return reponse(connecte, gerer.modifierDiffusion(Acteurs.de(connecte), id, requete.versSaisie()));
    }

    @PutMapping("/{id}/designations")
    @Operation(operationId = "designerContactsOperateur",
            summary = "Désigner le contact principal et le destinataire des factures (RG-OPE-22, 23, 24)")
    FicheOperateurResponse designer(Connecte connecte, @PathVariable long id,
            @RequestBody SaisieDesignationsRequest requete) {
        return reponse(connecte, gerer.designer(Acteurs.de(connecte), id, requete.versSaisie()));
    }

    @PostMapping("/{id}/nouveau-code")
    @Operation(operationId = "attribuerNouveauCodeOperateur",
            summary = "Attribuer le code proposé pour le type et le code postal actuels (RG-OPE-05)")
    FicheOperateurResponse attribuerNouveauCode(Connecte connecte, @PathVariable long id) {
        return reponse(connecte, gerer.attribuerNouveauCode(Acteurs.de(connecte), id));
    }

    @PostMapping("/{id}/desactivation")
    @Operation(operationId = "desactiverOperateur",
            summary = "Désactiver un opérateur (ADMIN BNIA) : consultable, plus proposé ; rattachements gelés, invitations annulées (RG-OPE-26)")
    FicheOperateurResponse desactiver(Connecte connecte, @PathVariable long id) {
        return reponse(connecte, gerer.desactiver(Acteurs.de(connecte), id));
    }

    @PostMapping("/{id}/reactivation")
    @Operation(operationId = "reactiverOperateur", summary = "Réactiver un opérateur (ADMIN BNIA, RG-OPE-26)")
    FicheOperateurResponse reactiver(Connecte connecte, @PathVariable long id) {
        return reponse(connecte, gerer.reactiver(Acteurs.de(connecte), id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "supprimerOperateur",
            summary = "Supprimer un opérateur que rien ne référence, avec ses personnes (ADMIN BNIA, RG-OPE-25, 44)")
    void supprimer(Connecte connecte, @PathVariable long id) {
        gerer.supprimer(Acteurs.de(connecte), id);
    }

    private FicheOperateurResponse reponse(Connecte connecte, Operateur operateur) {
        return fiche(connecte, operateur.id());
    }

    private FicheOperateurResponse fiche(Connecte connecte, long id) {
        return FicheOperateurResponse.depuis(gerer.fiche(Acteurs.de(connecte), id));
    }
}
