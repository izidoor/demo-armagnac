package fr.bnia.operateurs.adapter.rest;

import fr.bnia.commun.adapter.rest.Ressource;
import fr.bnia.operateurs.application.GererContacts;
import fr.bnia.operateurs.domain.Canal;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Contacts d'un opérateur au back-office : le BNIA peut tout, RG-OPE-30 ne le bride pas. */
@RestController
@RequestMapping("/api/operateurs/{operateur}/contacts")
@Tag(name = "contacts", description = "Opérateurs : contacts et droits au portail (§4.3.4, C.1.9)")
@Ressource(singulier = "Contact", pluriel = "Contacts")
class ContactController extends ContactsController {

    ContactController(GererContacts gerer) {
        super(gerer, Canal.BACK_OFFICE);
    }
}
