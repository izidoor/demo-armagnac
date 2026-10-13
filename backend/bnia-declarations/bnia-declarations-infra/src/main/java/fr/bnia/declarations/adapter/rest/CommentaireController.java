package fr.bnia.declarations.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.declarations.application.CommenterDsm;
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

/** Commentaires d'instruction d'une DSM (spec 05 §3.2.4, §6.3), mêmes endpoints des deux côtés (RG-DEC-01). */
@RestController
@RequestMapping("/api/operateurs/{operateur}/dsm/{dsm}/commentaires")
@Tag(name = "declarations")
class CommentaireController {

    private final CommenterDsm commenter;

    CommentaireController(CommenterDsm commenter) {
        this.commenter = commenter;
    }

    @GetMapping
    @Operation(operationId = "listerCommentairesDsm",
            summary = "Le fil des commentaires, du plus ancien au plus récent ; sans les internes pour l'opérateur (RG-DEC-24)")
    List<CommentaireResponse> lister(Connecte connecte, @PathVariable long operateur, @PathVariable long dsm) {
        return commenter.lister(Acteurs.de(connecte), operateur, dsm).stream()
                .map(c -> CommentaireResponse.depuis(c, connecte.utilisateur())).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "ecrireCommentaireDsm", summary = "Commenter la DSM ou une de ses lignes (RG-DEC-24)")
    CommentaireResponse ecrire(Connecte connecte, @PathVariable long operateur, @PathVariable long dsm,
            @RequestBody CommentaireRequest requete) {
        return CommentaireResponse.depuis(commenter.ecrire(Acteurs.de(connecte), operateur, dsm, requete.ligne(),
                requete.texte(), requete.estInterne()), connecte.utilisateur());
    }

    @PutMapping("/{id}")
    @Operation(operationId = "modifierCommentaireDsm", summary = "L'auteur modifie son commentaire (RG-DEC-25)")
    CommentaireResponse modifier(Connecte connecte, @PathVariable long operateur, @PathVariable long dsm,
            @PathVariable long id, @RequestBody CommentaireRequest requete) {
        return CommentaireResponse.depuis(commenter.modifier(Acteurs.de(connecte), operateur, dsm, id, requete.texte(),
                requete.estInterne()), connecte.utilisateur());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "supprimerCommentaireDsm",
            summary = "L'auteur supprime son commentaire, sauf un motif de compléments (RG-DEC-25)")
    void supprimer(Connecte connecte, @PathVariable long operateur, @PathVariable long dsm, @PathVariable long id) {
        commenter.supprimer(Acteurs.de(connecte), operateur, dsm, id);
    }
}
