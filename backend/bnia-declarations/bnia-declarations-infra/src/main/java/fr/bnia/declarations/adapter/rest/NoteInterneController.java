package fr.bnia.declarations.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.declarations.application.AnnoterDsm;
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

/** Carnet de notes internes d'une DSM (spec 15 §3.7), au personnel du BNIA seulement (RG-DEC-01, 24). */
@RestController
@RequestMapping("/api/operateurs/{operateur}/dsm/{dsm}/notes")
@Tag(name = "declarations")
class NoteInterneController {

    private final AnnoterDsm annoter;

    NoteInterneController(AnnoterDsm annoter) {
        this.annoter = annoter;
    }

    @GetMapping
    @Operation(operationId = "listerNotesInternesDsm",
            summary = "Les notes internes de la DSM, du plus ancien au plus récent ; personnel du BNIA (RG-DEC-24)")
    List<NoteInterneResponse> lister(Connecte connecte, @PathVariable long operateur, @PathVariable long dsm) {
        return annoter.lister(Acteurs.de(connecte), operateur, dsm).stream()
                .map(n -> NoteInterneResponse.depuis(n, connecte.utilisateur())).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "ecrireNoteInterneDsm", summary = "Annoter la DSM ou une de ses lignes (RG-DEC-24)")
    NoteInterneResponse ecrire(Connecte connecte, @PathVariable long operateur, @PathVariable long dsm,
            @RequestBody NoteInterneRequest requete) {
        return NoteInterneResponse.depuis(annoter.ecrire(Acteurs.de(connecte), operateur, dsm, requete.ligne(),
                requete.texte()), connecte.utilisateur());
    }

    @PutMapping("/{id}")
    @Operation(operationId = "modifierNoteInterneDsm", summary = "L'auteur modifie sa note (RG-DEC-24)")
    NoteInterneResponse modifier(Connecte connecte, @PathVariable long operateur, @PathVariable long dsm,
            @PathVariable long id, @RequestBody NoteInterneRequest requete) {
        return NoteInterneResponse.depuis(annoter.modifier(Acteurs.de(connecte), operateur, dsm, id, requete.texte()),
                connecte.utilisateur());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "supprimerNoteInterneDsm", summary = "L'auteur supprime sa note (RG-DEC-24)")
    void supprimer(Connecte connecte, @PathVariable long operateur, @PathVariable long dsm, @PathVariable long id) {
        annoter.supprimer(Acteurs.de(connecte), operateur, dsm, id);
    }
}
