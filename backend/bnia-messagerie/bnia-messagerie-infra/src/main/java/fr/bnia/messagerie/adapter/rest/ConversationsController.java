package fr.bnia.messagerie.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.commun.application.StockageDocuments.DocumentLu;
import fr.bnia.messagerie.api.ObjetRef;
import fr.bnia.messagerie.api.TypeObjet;
import fr.bnia.messagerie.application.Converser;
import fr.bnia.messagerie.application.FichierJoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Conversations et messages (spec 15 §5.1), mêmes endpoints au back-office et au portail : le côté se déduit des
 * droits de l'utilisateur sur l'opérateur de la conversation (RG-MSG-03). Afficher une conversation la lit pour ce
 * côté (RG-MSG-10).
 */
@RestController
@RequestMapping("/api")
@Tag(name = "messagerie")
class ConversationsController {

    private final Converser converser;

    ConversationsController(Converser converser) {
        this.converser = converser;
    }

    @GetMapping("/messagerie/objets/{type}/{identifiant}")
    @Operation(operationId = "consulterConversationObjet",
            summary = "La conversation d'un objet, vide s'il n'en a pas encore ; affichée, elle est lue (RG-MSG-01, 04, 10)")
    ConversationResponse consulterObjet(Connecte connecte, @PathVariable TypeObjet type,
            @PathVariable String identifiant) {
        return ConversationResponse.depuis(converser.consulterObjet(Acteurs.de(connecte), new ObjetRef(type, identifiant)),
                connecte.utilisateur());
    }

    @PostMapping("/messagerie/objets/{type}/{identifiant}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "ecrireMessageObjet",
            summary = "Écrire sur un objet ; sa conversation naît au premier message (RG-MSG-01, 04, 05)")
    MessageResponse ecrireSurObjet(Connecte connecte, @PathVariable TypeObjet type, @PathVariable String identifiant,
            @RequestBody SaisieMessageRequest requete) {
        return MessageResponse.depuis(converser.ecrireSurObjet(Acteurs.de(connecte), new ObjetRef(type, identifiant),
                requete.texte()), false, connecte.utilisateur());
    }

    @GetMapping("/messagerie/conversations/{id}")
    @Operation(operationId = "consulterConversation",
            summary = "Une conversation ; affichée, elle est lue pour mon côté (RG-MSG-01, 02, 10)")
    ConversationResponse consulter(Connecte connecte, @PathVariable long id) {
        return ConversationResponse.depuis(converser.consulter(Acteurs.de(connecte), id), connecte.utilisateur());
    }

    @PostMapping(path = "/messagerie/conversations/{id}/messages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "repondreConversation",
            summary = "Répondre dans une conversation ; pièces jointes en conversation libre (RG-MSG-01, 02, 05, 13, 14)")
    MessageResponse repondre(Connecte connecte, @PathVariable long id, @RequestPart("texte") String texte,
            @RequestPart(name = "pieces", required = false) List<MultipartFile> pieces) {
        return MessageResponse.depuis(converser.ecrire(Acteurs.de(connecte), id, texte, fichiers(pieces)), false,
                connecte.utilisateur());
    }

    @PostMapping(path = "/operateurs/{operateur}/messagerie/conversations",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "ouvrirConversation",
            summary = "Portail : ouvrir une conversation libre avec le BNIA, que je suis seul à voir dans mon opérateur"
                    + " (RG-MSG-02, 05, 12, 14)")
    ConversationResponse ouvrir(Connecte connecte, @PathVariable long operateur, @RequestPart("titre") String titre,
            @RequestPart("texte") String texte,
            @RequestPart(name = "pieces", required = false) List<MultipartFile> pieces) {
        return ConversationResponse.depuis(converser.ouvrirLibre(Acteurs.de(connecte), operateur, titre, texte,
                fichiers(pieces)), connecte.utilisateur());
    }

    @GetMapping("/messagerie/conversations/{id}/pieces/{cle}")
    @Operation(operationId = "telechargerPieceJointe",
            summary = "Une pièce jointe d'une conversation que je peux lire (RG-MSG-14)")
    ResponseEntity<byte[]> piece(Connecte connecte, @PathVariable long id, @PathVariable long cle) {
        DocumentLu piece = converser.piece(Acteurs.de(connecte), id, cle);
        MediaType type = MediaType.parseMediaType(piece.document().type());
        // PDF et images s'ouvrent dans le navigateur ; un classeur se télécharge.
        ContentDisposition disposition = (type.getType().equals("image") || type.equals(MediaType.APPLICATION_PDF)
                ? ContentDisposition.inline() : ContentDisposition.attachment())
                .filename(piece.document().nom(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok().contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(piece.contenu());
    }

    @PutMapping("/messagerie/messages/{id}")
    @Operation(operationId = "modifierMessage", summary = "L'auteur modifie son message (RG-MSG-06)")
    MessageResponse modifier(Connecte connecte, @PathVariable long id, @RequestBody SaisieMessageRequest requete) {
        return MessageResponse.depuis(converser.modifier(Acteurs.de(connecte), id, requete.texte()), false,
                connecte.utilisateur());
    }

    @DeleteMapping("/messagerie/messages/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "supprimerMessage",
            summary = "L'auteur supprime son message, sauf un message protégé ; sa conversation, s'il était le dernier (RG-MSG-06)")
    void supprimer(Connecte connecte, @PathVariable long id) {
        converser.supprimer(Acteurs.de(connecte), id);
    }

    @PostMapping("/messagerie/messages/{id}/non-lu")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "marquerMessageNonLu",
            summary = "Remettre un message non lu pour mon côté, jusqu'au prochain affichage (RG-MSG-10)")
    void marquerNonLu(Connecte connecte, @PathVariable long id) {
        converser.marquerNonLu(Acteurs.de(connecte), id);
    }

    private static List<FichierJoint> fichiers(List<MultipartFile> pieces) {
        if (pieces == null) {
            return List.of();
        }
        return pieces.stream().filter(p -> !p.isEmpty()).map(p -> {
            try {
                return new FichierJoint(p.getOriginalFilename() == null ? "piece" : p.getOriginalFilename(), p.getBytes());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).toList();
    }
}
