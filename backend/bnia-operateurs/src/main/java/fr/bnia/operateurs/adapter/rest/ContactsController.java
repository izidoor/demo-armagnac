package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.GererContacts;
import fr.bnia.operateurs.domain.Canal;
import fr.bnia.operateurs.domain.Contact;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Endpoints des contacts, communs au back-office et au portail (§4.3.4, §5.13, C.1.9). Chaque
 * sous-classe fixe le chemin, le canal et la {@link fr.bnia.commun.adapter.rest.Ressource} qui nomme ses
 * opérations.
 */
abstract class ContactsController {

    private final GererContacts gerer;
    private final Canal canal;

    protected ContactsController(GererContacts gerer, Canal canal) {
        this.gerer = gerer;
        this.canal = canal;
    }

    /** Contrôle d'accès propre au canal, avant toute opération. */
    protected void verifierAcces(long operateur) {
    }

    @GetMapping("/{contact}")
    @Operation(summary = "Consulter un contact, ses droits et ses désignations")
    ContactResponse consulter(@PathVariable long operateur, @PathVariable long contact) {
        verifierAcces(operateur);
        return ContactResponse.depuis(gerer.consulter(operateur, contact));
    }

    @GetMapping("/{contact}/journal")
    @Operation(summary = "Journal d'un contact, du plus récent au plus ancien (RG-OPE-34)")
    List<LigneJournalResponse> consulterJournal(@PathVariable long operateur, @PathVariable long contact) {
        verifierAcces(operateur);
        return gerer.journal(operateur, contact).stream().map(LigneJournalResponse::depuis).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un contact, actif, sans accès (RG-OPE-21, 27, 28)")
    ContactResponse creer(@PathVariable long operateur, @RequestBody SaisieContactRequest requete) {
        verifierAcces(operateur);
        return reponse(operateur, gerer.creer(operateur, requete.versSaisie(), canal));
    }

    @PutMapping("/{contact}")
    @Operation(summary = "Modifier un contact (RG-OPE-21, 27, 28, 29, 32)")
    ContactResponse modifier(@PathVariable long operateur, @PathVariable long contact,
            @RequestBody SaisieContactRequest requete) {
        verifierAcces(operateur);
        return reponse(operateur, gerer.modifier(operateur, contact, requete.versSaisie(), canal));
    }

    @PutMapping("/{contact}/portail")
    @Operation(summary = "Accès au portail, rôle d'administrateur, modules (RG-OPE-29 à 31)")
    ContactResponse modifierDroits(@PathVariable long operateur, @PathVariable long contact,
            @RequestBody SaisieDroitsRequest requete) {
        verifierAcces(operateur);
        return reponse(operateur, gerer.modifierDroits(operateur, contact, requete.versSaisie(), canal));
    }

    @PostMapping("/{contact}/desactivation")
    @Operation(summary = "Désactiver : retire l'accès et les désignations (RG-OPE-33)")
    ContactResponse desactiver(@PathVariable long operateur, @PathVariable long contact) {
        verifierAcces(operateur);
        return reponse(operateur, gerer.desactiver(operateur, contact, canal));
    }

    @PostMapping("/{contact}/reactivation")
    @Operation(summary = "Réactiver, sans accès (RG-OPE-33)")
    ContactResponse reactiver(@PathVariable long operateur, @PathVariable long contact) {
        verifierAcces(operateur);
        return reponse(operateur, gerer.reactiver(operateur, contact, canal));
    }

    @DeleteMapping("/{contact}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer : comme la désactivation ; le journal reste (RG-OPE-33, 34)")
    void supprimer(@PathVariable long operateur, @PathVariable long contact) {
        verifierAcces(operateur);
        gerer.supprimer(operateur, contact, canal);
    }

    private ContactResponse reponse(long operateur, Contact contact) {
        return ContactResponse.depuis(gerer.consulter(operateur, contact.id()));
    }
}
