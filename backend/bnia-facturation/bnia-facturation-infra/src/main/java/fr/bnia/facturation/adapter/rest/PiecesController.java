package fr.bnia.facturation.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.commun.application.StockageDocuments.DocumentLu;
import fr.bnia.facturation.adapter.rest.BrouillonsRequests.ArticleProposeResponse;
import fr.bnia.facturation.adapter.rest.BrouillonsRequests.CompteRenduResponse;
import fr.bnia.facturation.adapter.rest.BrouillonsRequests.EmissionRequest;
import fr.bnia.facturation.adapter.rest.BrouillonsRequests.NoteRequest;
import fr.bnia.facturation.application.AbandonnerBrouillon;
import fr.bnia.facturation.application.AnnoterPiece;
import fr.bnia.facturation.application.ConsulterFactures;
import fr.bnia.facturation.application.EmettreLot;
import fr.bnia.facturation.application.EmettrePiece;
import fr.bnia.facturation.application.ModifierBrouillon;
import fr.bnia.facturation.application.FiltrePieces;
import fr.bnia.facturation.application.VuePiece;
import fr.bnia.facturation.domain.EtatPiece;
import fr.bnia.facturation.domain.SensPiece;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
 * Les pièces au back-office : historique, détail, PDF, journal, notes ; brouillons modifiés, émis seuls ou en lot,
 * abandonnés (UC-FAC-2, UC-FAC-4).
 */
@RestController
@RequestMapping("/api/facturation/pieces")
@Tag(name = "facturation")
class PiecesController {

    private final ConsulterFactures consulter;
    private final EmettrePiece emettre;
    private final EmettreLot emettreLot;
    private final ModifierBrouillon modifier;
    private final AbandonnerBrouillon abandonner;
    private final AnnoterPiece annoter;

    PiecesController(ConsulterFactures consulter, EmettrePiece emettre, EmettreLot emettreLot,
            ModifierBrouillon modifier, AbandonnerBrouillon abandonner, AnnoterPiece annoter) {
        this.consulter = consulter;
        this.emettre = emettre;
        this.emettreLot = emettreLot;
        this.modifier = modifier;
        this.abandonner = abandonner;
        this.annoter = annoter;
    }

    @GetMapping
    @Operation(operationId = "listerPieces",
            summary = "L'historique des pièces, brouillons compris, filtré (RG-FAC-26, 27)")
    List<PieceListeeResponse> lister(Connecte connecte,
            @Parameter(description = "Début du numéro") @RequestParam(required = false) String numero,
            @RequestParam(required = false) SensPiece sens,
            @RequestParam(required = false) EtatPiece etat,
            @Parameter(description = "Nature d'origine", example = "DSM") @RequestParam(required = false) String nature,
            @Parameter(description = "Identifiant du client (opérateur)") @RequestParam(required = false) String client,
            @Parameter(description = "Contenu dans le nom de l'acheteur ou le n° BNIA du client")
            @RequestParam(required = false) String acheteur,
            @Parameter(description = "Contenu dans le libellé de la période") @RequestParam(required = false) String periode,
            @Parameter(description = "Date d'émission au plus tôt")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
            @Parameter(description = "Date d'émission au plus tard")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) {
        FiltrePieces filtre = new FiltrePieces(texte(numero), Optional.ofNullable(sens), Optional.ofNullable(etat),
                texte(nature), texte(client), texte(acheteur), texte(periode), Optional.ofNullable(du),
                Optional.ofNullable(au));
        return consulter.lister(Acteurs.de(connecte), filtre).stream().map(PieceListeeResponse::de).toList();
    }

    @GetMapping("/{id}")
    @Operation(operationId = "consulterPiece",
            summary = "Une pièce ; pour un brouillon, ce qui empêche son émission (RG-FAC-09, 20, 26)")
    PieceResponse consulter(Connecte connecte, @PathVariable long id) {
        return PieceResponse.de(consulter.consulter(Acteurs.de(connecte), id));
    }

    @GetMapping(path = "/{id}/apercu", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(operationId = "apercuPdfBrouillon",
            summary = "L'aperçu PDF d'un brouillon, marqué « projet », ni conservé ni journalisé (RG-FAC-15)")
    ResponseEntity<byte[]> apercu(Connecte connecte, @PathVariable long id) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename("PROJET-" + id + ".pdf", StandardCharsets.UTF_8).build().toString())
                .body(consulter.apercu(Acteurs.de(connecte), id));
    }

    @GetMapping(path = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(operationId = "telechargerPdfPiece", summary = "Le PDF d'une pièce émise, journalisé (RG-FAC-15, 20)")
    ResponseEntity<byte[]> pdf(Connecte connecte, @PathVariable long id) {
        DocumentLu pdf = consulter.pdf(Acteurs.de(connecte), id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(pdf.document().nom(), StandardCharsets.UTF_8).build().toString())
                .body(pdf.contenu());
    }

    @GetMapping("/{id}/journal")
    @Operation(operationId = "consulterJournalPiece",
            summary = "Le journal d'une pièce, du plus récent au plus ancien (RG-FAC-20, 26)")
    List<EntreeJournalPieceResponse> journal(Connecte connecte, @PathVariable long id) {
        return consulter.journal(Acteurs.de(connecte), id).stream().map(EntreeJournalPieceResponse::de).toList();
    }

    @PostMapping("/{id}/emission")
    @Operation(operationId = "emettrePiece",
            summary = "Émet un brouillon : numéro, échéance, vendeur recopié, PDF, FactureEmise (RG-FAC-09 à 15, 25)")
    PieceResponse emettre(Connecte connecte, @PathVariable long id) {
        return PieceResponse.de(new VuePiece(emettre.emettre(Acteurs.de(connecte), id), List.of(), true));
    }

    @PostMapping("/emission")
    @Operation(operationId = "emettreBrouillons",
            summary = "Émet une sélection de brouillons, chacun dans sa transaction ; compte rendu par brouillon (RG-FAC-19)")
    List<CompteRenduResponse> emettreLot(Connecte connecte, @RequestBody EmissionRequest selection) {
        return emettreLot.emettre(Acteurs.de(connecte), selection.ids() == null ? List.of() : selection.ids()).stream()
                .map(CompteRenduResponse::de).toList();
    }

    @PutMapping("/{id}")
    @Operation(operationId = "modifierBrouillon",
            summary = "Enregistre l'en-tête et les lignes d'un brouillon ; lignes reçues verrouillées (RG-FAC-17)")
    PieceResponse modifier(Connecte connecte, @PathVariable long id, @RequestBody ModificationBrouillonRequest saisie) {
        return PieceResponse.de(modifier.modifier(Acteurs.de(connecte), id, saisie.versSaisie()));
    }

    @GetMapping("/{id}/articles")
    @Operation(operationId = "listerArticlesProposes",
            summary = "Les articles actifs de tous les catalogues, à la date de l'opération du brouillon (RG-FAC-03)")
    List<ArticleProposeResponse> articles(Connecte connecte, @PathVariable long id) {
        return modifier.articlesProposes(Acteurs.de(connecte), id).stream().map(ArticleProposeResponse::de).toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "abandonnerBrouillon",
            summary = "Abandonne un brouillon : supprimé avec son journal, FactureAbandonnee publiée (RG-FAC-18)")
    void abandonner(Connecte connecte, @PathVariable long id) {
        abandonner.abandonner(Acteurs.de(connecte), id);
    }

    @PostMapping("/{id}/notes")
    @Operation(operationId = "annoterPiece", summary = "Ajoute une note interne au journal de la pièce (RG-FAC-21)")
    EntreeJournalPieceResponse annoter(Connecte connecte, @PathVariable long id, @RequestBody NoteRequest note) {
        return EntreeJournalPieceResponse.de(annoter.annoter(Acteurs.de(connecte), id, note.texte()));
    }

    private static Optional<String> texte(String valeur) {
        return Optional.ofNullable(valeur).map(String::strip).filter(v -> !v.isEmpty());
    }
}
