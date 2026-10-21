package fr.bnia.facturation.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.facturation.adapter.rest.BrouillonsRequests.NoteRequest;
import fr.bnia.facturation.application.AnnoterPiece;
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

/** Carnet de notes internes d'une pièce, au personnel du BNIA seulement (RG-FAC-21, 26). */
@RestController
@RequestMapping("/api/facturation/pieces/{piece}/notes")
@Tag(name = "facturation")
class NotesPieceController {

    private final AnnoterPiece annoter;

    NotesPieceController(AnnoterPiece annoter) {
        this.annoter = annoter;
    }

    @GetMapping
    @Operation(operationId = "listerNotesInternesPiece",
            summary = "Les notes internes de la pièce, du plus ancien au plus récent ; personnel du BNIA (RG-FAC-21)")
    List<NoteInternePieceResponse> lister(Connecte connecte, @PathVariable long piece) {
        return annoter.lister(Acteurs.de(connecte), piece).stream()
                .map(n -> NoteInternePieceResponse.depuis(n, connecte.utilisateur())).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "ecrireNoteInternePiece",
            summary = "Annoter la pièce, quel que soit son état ; jamais imprimée ni transmise (RG-FAC-21)")
    NoteInternePieceResponse ecrire(Connecte connecte, @PathVariable long piece, @RequestBody NoteRequest requete) {
        return NoteInternePieceResponse.depuis(annoter.ecrire(Acteurs.de(connecte), piece, requete.texte()),
                connecte.utilisateur());
    }

    @PutMapping("/{id}")
    @Operation(operationId = "modifierNoteInternePiece", summary = "L'auteur modifie sa note (RG-FAC-21)")
    NoteInternePieceResponse modifier(Connecte connecte, @PathVariable long piece, @PathVariable long id,
            @RequestBody NoteRequest requete) {
        return NoteInternePieceResponse.depuis(annoter.modifier(Acteurs.de(connecte), piece, id, requete.texte()),
                connecte.utilisateur());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "supprimerNoteInternePiece", summary = "L'auteur supprime sa note (RG-FAC-21)")
    void supprimer(Connecte connecte, @PathVariable long piece, @PathVariable long id) {
        annoter.supprimer(Acteurs.de(connecte), piece, id);
    }
}
