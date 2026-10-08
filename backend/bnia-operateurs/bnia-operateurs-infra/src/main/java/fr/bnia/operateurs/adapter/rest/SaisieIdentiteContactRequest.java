package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.SaisieIdentiteContact;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SaisieIdentiteContact", description = "Identité et e-mail d'un contact (vision UC-11) ; sa fiche se modifie à part")
public record SaisieIdentiteContactRequest(
        @Schema(description = "Identifiant du titre") Long titre,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Petit") String nom,
        @Schema(example = "Paul") String prenom,
        @Schema(description = "Unique parmi les contacts de l'opérateur (RG-OPE-28)") String email,
        @Schema(description = "Version lue (RG-OPE-51)") Long version) {

    SaisieIdentiteContact versSaisie() {
        return new SaisieIdentiteContact(titre, nom, prenom, email, version);
    }
}
