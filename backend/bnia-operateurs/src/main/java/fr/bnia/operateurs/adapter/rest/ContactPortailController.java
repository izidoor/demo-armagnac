package fr.bnia.operateurs.adapter.rest;

import fr.bnia.commun.adapter.rest.Ressource;
import fr.bnia.operateurs.application.GererContacts;
import fr.bnia.operateurs.application.GererOperateurs;
import fr.bnia.operateurs.domain.Canal;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * « Contacts » au portail (§5.13, RG-OPE-35) : réservé à l'administrateur de l'opérateur ; aucune action
 * ne laisse l'opérateur sans administrateur actif (RG-OPE-30). Tant que {@code acces} n'existe pas,
 * l'utilisateur du portail administre l'opérateur de démonstration.
 */
@RestController
@RequestMapping("/api/portail/operateurs/{operateur}/contacts")
@Tag(name = "portail-contacts", description = "Portail : contacts de l'opérateur (§5.13)")
@Ressource(singulier = "ContactPortail", pluriel = "ContactsPortail")
class ContactPortailController extends ContactsController {

    private final GererOperateurs operateurs;

    ContactPortailController(GererContacts gerer, GererOperateurs operateurs) {
        super(gerer, Canal.PORTAIL);
        this.operateurs = operateurs;
    }

    @Override
    protected void verifierAcces(long operateur) {
        operateurs.verifierAccesPortail(operateur);
    }
}
