package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.SaisieContact;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(name = "SaisieContact", description = "Création ou modification d'un contact (C.1.9.1)")
public record SaisieContactRequest(
        @Schema(description = "Identifiant du titre") Long titre,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Durand") String nom,
        @Schema(example = "Jeanne") String prenom,
        @Schema(example = "Comptable") String fonction,
        @Schema(description = "Identifiants des centres d'intérêt") Set<Long> centresInteret,
        String telephoneProfessionnel,
        String telephoneSecondaire,
        @Schema(description = "Unique dans l'opérateur (RG-OPE-28) ; non modifiable s'il est l'identifiant d'un compte (RG-OPE-32)",
                example = "jeanne.durand@exemple.fr") String email,
        String adresse,
        String codePostal,
        String ville,
        @Schema(description = "Identifiant du pays") Long pays,
        String observation) {

    SaisieContact versSaisie() {
        return new SaisieContact(titre, nom, prenom, fonction, centresInteret, telephoneProfessionnel,
                telephoneSecondaire, email, adresse, codePostal, ville, pays, observation);
    }
}
