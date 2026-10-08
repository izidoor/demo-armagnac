package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.SaisieDesignations;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SaisieDesignations", description = "Contact principal et destinataire des factures (RG-OPE-22, 23)")
public record SaisieDesignationsRequest(
        @Schema(description = "Fiche professionnelle d'une personne de l'annuaire ; absent : aucun") Long contactPrincipal,
        @Schema(description = "Fiche professionnelle d'une personne de l'annuaire qui a un e-mail") Long destinataireFacturesFiche,
        @Schema(description = "Adresse saisie, si le destinataire n'est pas une personne de l'annuaire") String destinataireFacturesEmail,
        @Schema(description = "Version de l'opérateur lue (RG-OPE-51)") Long version) {

    SaisieDesignations versSaisie() {
        return new SaisieDesignations(contactPrincipal, destinataireFacturesFiche, destinataireFacturesEmail, version);
    }
}
