package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.SaisieDesignations;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SaisieDesignations", description = "Contact principal et destinataire des factures (RG-OPE-22, 23)")
public record SaisieDesignationsRequest(
        @Schema(description = "Identifiant d'un contact actif ; absent : aucun") Long contactPrincipal,
        @Schema(description = "Identifiant d'un contact actif avec un e-mail") Long destinataireFacturesContact,
        @Schema(description = "Adresse saisie, si le destinataire n'est pas un contact") String destinataireFacturesEmail) {

    SaisieDesignations versSaisie() {
        return new SaisieDesignations(contactPrincipal, destinataireFacturesContact, destinataireFacturesEmail);
    }
}
