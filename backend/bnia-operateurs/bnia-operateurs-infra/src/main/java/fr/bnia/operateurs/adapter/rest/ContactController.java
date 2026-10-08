package fr.bnia.operateurs.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.operateurs.application.ConsulterAnnuaire;
import fr.bnia.operateurs.application.GererContacts;
import fr.bnia.operateurs.domain.TypeCible;
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

/** Contacts d'un opérateur (vision UC-10, 11 ; §4.3.4, C.1.9), module Contacts (RG-OPE-31). */
@RestController
@RequestMapping("/api/operateurs/{operateur}/contacts")
@Tag(name = "contacts", description = "Opérateurs : contacts, personnes sans compte (§4.3.4, C.1.9)")
class ContactController {

    private final GererContacts gerer;
    private final ConsulterAnnuaire annuaire;

    ContactController(GererContacts gerer, ConsulterAnnuaire annuaire) {
        this.gerer = gerer;
        this.annuaire = annuaire;
    }

    @GetMapping("/{contact}")
    @Operation(operationId = "consulterContact", summary = "Consulter un contact, sa fiche et ses désignations")
    ContactResponse consulter(Connecte connecte, @PathVariable long operateur, @PathVariable long contact) {
        return ContactResponse.depuis(gerer.consulter(Acteurs.de(connecte), operateur, contact));
    }

    @GetMapping("/{contact}/journal")
    @Operation(operationId = "consulterJournalContact", summary = "Journal d'un contact, du plus récent au plus ancien (RG-OPE-34)")
    List<LigneJournalResponse> consulterJournal(Connecte connecte, @PathVariable long operateur,
            @PathVariable long contact) {
        return annuaire.journalDe(Acteurs.de(connecte), operateur, TypeCible.CONTACT, contact).stream()
                .map(LigneJournalResponse::depuis).toList();
    }

    @GetMapping("/avertissements")
    @Operation(operationId = "listerAvertissementsContact",
            summary = "E-mail saisi qui est celui d'un membre ou d'une invitation en attente : avertissements non bloquants (RG-OPE-28)")
    List<String> avertissements(Connecte connecte, @PathVariable long operateur, @RequestParam String email,
            @Parameter(description = "Contact en cours de modification") @RequestParam(required = false) Long sauf) {
        return gerer.avertissements(Acteurs.de(connecte), operateur, email, sauf);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "creerContact", summary = "Déclarer un contact et sa fiche (RG-OPE-21, 27, 28, 30)")
    ContactResponse creer(Connecte connecte, @PathVariable long operateur, @RequestBody SaisieContactRequest requete) {
        long id = gerer.creer(Acteurs.de(connecte), operateur, requete.versSaisie()).id();
        return consulter(connecte, operateur, id);
    }

    @PutMapping("/{contact}")
    @Operation(operationId = "modifierContact", summary = "Modifier l'identité et l'e-mail d'un contact (RG-OPE-21, 23, 27, 28, 51)")
    ContactResponse modifier(Connecte connecte, @PathVariable long operateur, @PathVariable long contact,
            @RequestBody SaisieIdentiteContactRequest requete) {
        gerer.modifier(Acteurs.de(connecte), operateur, contact, requete.versSaisie());
        return consulter(connecte, operateur, contact);
    }

    @DeleteMapping("/{contact}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "supprimerContact",
            summary = "Supprimer un contact et sa fiche ; ses invitations en attente sont annulées (RG-OPE-22, 23, 45)")
    void supprimer(Connecte connecte, @PathVariable long operateur, @PathVariable long contact) {
        gerer.supprimer(Acteurs.de(connecte), operateur, contact);
    }
}
