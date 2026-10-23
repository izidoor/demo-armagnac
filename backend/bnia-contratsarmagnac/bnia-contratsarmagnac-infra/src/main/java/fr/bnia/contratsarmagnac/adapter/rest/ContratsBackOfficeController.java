package fr.bnia.contratsarmagnac.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.contratsarmagnac.application.ConsulterContrats;
import fr.bnia.contratsarmagnac.application.EditerContrats;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Contrats au back-office (spec 06 §6.1) : tous, en lecture, pour le personnel du BNIA (RG-CA-01). */
@RestController
@RequestMapping("/api/contrats")
@Tag(name = "contratsarmagnac", description = "Contrats d'achat Armagnac (spec 06)")
class ContratsBackOfficeController {

    private final ConsulterContrats consulter;
    private final EditerContrats editer;

    ContratsBackOfficeController(ConsulterContrats consulter, EditerContrats editer) {
        this.consulter = consulter;
        this.editer = editer;
    }

    @GetMapping("/mercuriales")
    @Operation(operationId = "consulterMercuriales",
            summary = "Les mercuriales des contrats conclus ou exécutés de la période, par défaut les 12 derniers mois (RG-CA-01, 26)")
    MercurialesResponse mercuriales(Connecte connecte,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> du,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> au) {
        return MercurialesResponse.depuis(consulter.mercuriales(Acteurs.de(connecte), du, au));
    }

    @GetMapping
    @Operation(operationId = "listerContrats",
            summary = "Tous les contrats, filtrés par opérateur partie et par numéro (RG-CA-01, 25)")
    List<ContratListeResponse> lister(Connecte connecte, @RequestParam Optional<Long> operateur,
            @RequestParam Optional<String> numero) {
        return consulter.duBackOffice(Acteurs.de(connecte), operateur, numero).stream()
                .map(ContratListeResponse::depuis).toList();
    }

    @GetMapping("/{id}")
    @Operation(operationId = "consulterContrat", summary = "Un contrat, en lecture (RG-CA-01, 09, 19)")
    ContratResponse consulter(Connecte connecte, @PathVariable long id) {
        return ContratResponse.depuis(consulter.auBackOffice(Acteurs.de(connecte), id));
    }

    @GetMapping(path = "/{id}/pdf", produces = "application/pdf")
    @Operation(operationId = "editerContratPdf", summary = "Le PDF du contrat, au formulaire CA-05 allégé (RG-CA-01, 20)")
    ResponseEntity<byte[]> pdf(Connecte connecte, @PathVariable long id) {
        return Pdf.reponse(editer.auBackOffice(Acteurs.de(connecte), id));
    }
}
