package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.GererContacts;
import fr.bnia.operateurs.domain.Canal;
import fr.bnia.operateurs.domain.Contact;
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

/** Contacts d'un opérateur au back-office (§4.3.4, C.1.9) : le BNIA peut tout, RG-OPE-30 ne le bride pas. */
@RestController
@RequestMapping("/api/operateurs/{operateur}/contacts")
@Tag(name = "contacts", description = "Opérateurs : contacts et droits au portail (§4.3.4, C.1.9)")
class ContactController {

    private final GererContacts gerer;

    ContactController(GererContacts gerer) {
        this.gerer = gerer;
    }

    @GetMapping("/{contact}")
    @Operation(operationId = "consulterContact", summary = "Consulter un contact, ses droits et ses désignations")
    ContactResponse consulter(@PathVariable long operateur, @PathVariable long contact) {
        return ContactResponse.depuis(gerer.consulter(operateur, contact));
    }

    @GetMapping("/{contact}/journal")
    @Operation(operationId = "consulterJournalContact", summary = "Journal d'un contact, du plus récent au plus ancien (RG-OPE-34)")
    List<LigneJournalResponse> journal(@PathVariable long operateur, @PathVariable long contact) {
        return gerer.journal(operateur, contact).stream().map(LigneJournalResponse::depuis).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "creerContact", summary = "Créer un contact, actif, sans accès (RG-OPE-21, 27, 28)")
    ContactResponse creer(@PathVariable long operateur, @RequestBody SaisieContactRequest requete) {
        return reponse(operateur, gerer.creer(operateur, requete.versSaisie(), Canal.BACK_OFFICE));
    }

    @PutMapping("/{contact}")
    @Operation(operationId = "modifierContact", summary = "Modifier un contact (RG-OPE-21, 27, 28, 29, 32)")
    ContactResponse modifier(@PathVariable long operateur, @PathVariable long contact,
            @RequestBody SaisieContactRequest requete) {
        return reponse(operateur, gerer.modifier(operateur, contact, requete.versSaisie(), Canal.BACK_OFFICE));
    }

    @PutMapping("/{contact}/portail")
    @Operation(operationId = "modifierDroitsContact", summary = "Accès au portail, rôle d'administrateur, modules (RG-OPE-29 à 31)")
    ContactResponse modifierDroits(@PathVariable long operateur, @PathVariable long contact,
            @RequestBody SaisieDroitsRequest requete) {
        return reponse(operateur, gerer.modifierDroits(operateur, contact, requete.versSaisie(), Canal.BACK_OFFICE));
    }

    @PostMapping("/{contact}/desactivation")
    @Operation(operationId = "desactiverContact", summary = "Désactiver : retire l'accès et les désignations (RG-OPE-33)")
    ContactResponse desactiver(@PathVariable long operateur, @PathVariable long contact) {
        return reponse(operateur, gerer.desactiver(operateur, contact, Canal.BACK_OFFICE));
    }

    @PostMapping("/{contact}/reactivation")
    @Operation(operationId = "reactiverContact", summary = "Réactiver, sans accès (RG-OPE-33)")
    ContactResponse reactiver(@PathVariable long operateur, @PathVariable long contact) {
        return reponse(operateur, gerer.reactiver(operateur, contact, Canal.BACK_OFFICE));
    }

    @DeleteMapping("/{contact}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "supprimerContact", summary = "Supprimer : comme la désactivation ; le journal reste (RG-OPE-33, 34)")
    void supprimer(@PathVariable long operateur, @PathVariable long contact) {
        gerer.supprimer(operateur, contact, Canal.BACK_OFFICE);
    }

    private ContactResponse reponse(long operateur, Contact contact) {
        return ContactResponse.depuis(gerer.consulter(operateur, contact.id()));
    }
}
