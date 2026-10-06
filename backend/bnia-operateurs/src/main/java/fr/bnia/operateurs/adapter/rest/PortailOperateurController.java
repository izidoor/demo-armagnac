package fr.bnia.operateurs.adapter.rest;

import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.operateurs.application.GererContacts;
import fr.bnia.operateurs.application.GererOperateurs;
import fr.bnia.operateurs.application.GererPresentation;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.Canal;
import fr.bnia.operateurs.domain.Operateur;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Portail : « Mon entreprise » et la recherche d'un autre opérateur (spec 02, §6.2). L'opérateur est
 * désigné dans le chemin ; tant que {@code acces} n'existe pas, c'est l'opérateur de démonstration
 * ({@code bnia.portail.operateur-demo}, décision du 2026-10-06).
 */
@RestController
@RequestMapping("/api/portail")
@Tag(name = "portail-operateur", description = "Portail : profil de l'opérateur, recherche d'opérateurs (§5.13)")
class PortailOperateurController {

    private final GererOperateurs operateurs;
    private final GererContacts contacts;
    private final GererPresentation presentation;
    private final OperateursLecture lecture;
    private final String codeDemo;

    PortailOperateurController(GererOperateurs operateurs, GererContacts contacts, GererPresentation presentation,
            OperateursLecture lecture, @Value("${bnia.portail.operateur-demo:}") String codeDemo) {
        this.operateurs = operateurs;
        this.contacts = contacts;
        this.presentation = presentation;
        this.lecture = lecture;
        this.codeDemo = codeDemo;
    }

    @GetMapping("/operateur-demo")
    @Operation(operationId = "consulterOperateurDemoPortail",
            summary = "Opérateur ouvert au portail en attendant le contexte acces ; 404 hors profil demo")
    ResultatRechercheResponse operateurDemo() {
        return lecture.operateurParCode(codeDemo)
                .map(o -> new ResultatRechercheResponse(o.id(), o.code(), o.raisonSociale(), o.nomCommercial().orElse(null),
                        o.adresse().ville().orElse(null)))
                .orElseThrow(() -> new ElementIntrouvable("Aucun opérateur de démonstration."));
    }

    @GetMapping("/operateurs/recherche")
    @Operation(operationId = "rechercherOperateursPortail",
            summary = "Rechercher un autre opérateur : actifs, trois caractères au moins, vingt résultats au plus (RG-OPE-37)")
    List<ResultatRechercheResponse> rechercher(@RequestParam String texte) {
        return operateurs.rechercher(texte).stream().map(ResultatRechercheResponse::depuis).toList();
    }

    @GetMapping("/operateurs/{id}/profil")
    @Operation(operationId = "consulterProfilPortail", summary = "« Mon entreprise » : la fiche, sans le commentaire interne (RG-OPE-35, 36)")
    FicheOperateurResponse consulterProfil(@PathVariable long id) {
        operateurs.verifierAccesPortail(id);
        return profil(id);
    }

    @PutMapping("/operateurs/{id}/profil")
    @Operation(operationId = "modifierProfilPortail", summary = "Modifier les champs ouverts au portail (RG-OPE-20, 21, 24, 35, 36)")
    FicheOperateurResponse modifierProfil(@PathVariable long id, @RequestBody SaisieProfilPortailRequest requete) {
        return reponse(operateurs.modifierProfilPortail(id, requete.versSaisie()));
    }

    @PutMapping("/operateurs/{id}/diffusion")
    @Operation(operationId = "modifierDiffusionPortail", summary = "Modifier la diffusion des millésimes (RG-OPE-14, 36)")
    FicheOperateurResponse modifierDiffusion(@PathVariable long id, @RequestBody SaisieDiffusionPortailRequest requete) {
        return reponse(operateurs.modifierDiffusionPortail(id, requete.niveau(), requete.millesimes()));
    }

    @PutMapping("/operateurs/{id}/designations")
    @Operation(operationId = "designerContactsPortail",
            summary = "Désigner le contact principal et le destinataire des factures (RG-OPE-22, 23, 24, 36)")
    FicheOperateurResponse designer(@PathVariable long id, @RequestBody SaisieDesignationsRequest requete) {
        return reponse(contacts.designer(id, requete.versSaisie(), Canal.PORTAIL));
    }

    @GetMapping("/operateurs/{id}/presentation")
    @Operation(operationId = "consulterPresentationPortail", summary = "Consulter la présentation de l'opérateur")
    PresentationDto consulterPresentation(@PathVariable long id) {
        operateurs.verifierAccesPortail(id);
        return PresentationDto.depuis(presentation.consulter(id));
    }

    @PutMapping("/operateurs/{id}/presentation")
    @Operation(operationId = "modifierPresentationPortail", summary = "Remplacer la présentation (RG-OPE-16 à 19, 21, 35)")
    PresentationDto modifierPresentation(@PathVariable long id, @RequestBody PresentationDto requete) {
        return PresentationDto.depuis(presentation.modifier(id, requete.versSaisie(), Canal.PORTAIL));
    }

    private FicheOperateurResponse reponse(Operateur operateur) {
        return profil(operateur.id());
    }

    private FicheOperateurResponse profil(long id) {
        return FicheOperateurResponse.pourPortail(operateurs.fiche(id));
    }
}
