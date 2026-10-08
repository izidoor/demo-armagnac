package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.SaisieContact;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(name = "SaisieContact", description = "Déclaration d'un contact et de sa fiche professionnelle (vision UC-10, C.1.9.1)")
public record SaisieContactRequest(
        @Schema(description = "Identifiant du titre") Long titre,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Petit") String nom,
        @Schema(example = "Paul") String prenom,
        @Schema(description = "Unique parmi les contacts de l'opérateur (RG-OPE-28)", example = "paul.petit@exemple.fr")
        String email,
        @Schema(example = "Maître de chai") String fonction,
        String telephoneProfessionnel,
        String telephoneSecondaire,
        String adresse,
        String codePostal,
        String ville,
        @Schema(description = "Identifiant du pays") Long pays,
        @Schema(description = "Identifiants des centres d'intérêt") Set<Long> centresInteret,
        String observation) {

    SaisieContact versSaisie() {
        return new SaisieContact(titre, nom, prenom, email, fonction, telephoneProfessionnel, telephoneSecondaire,
                adresse, codePostal, ville, pays, centresInteret, observation);
    }
}
