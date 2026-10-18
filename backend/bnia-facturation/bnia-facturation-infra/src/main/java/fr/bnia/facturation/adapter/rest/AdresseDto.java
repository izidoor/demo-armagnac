package fr.bnia.facturation.adapter.rest;

import fr.bnia.facturation.domain.Adresse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AdresseFacturation", description = "Adresse d'un acheteur ou du vendeur (spec 08 §3.5, §3.7)")
public record AdresseDto(
        String ligne1,
        String ligne2,
        String codePostal,
        String ville,
        @Schema(description = "Nom du pays, imprimé", example = "France") String pays,
        @Schema(description = "Code ISO du pays", example = "FR") String codePays) {

    static AdresseDto de(Adresse a) {
        return a == null ? null : new AdresseDto(a.ligne1(), a.ligne2(), a.codePostal(), a.ville(), a.pays(), a.codePays());
    }

    Adresse versDomaine() {
        return new Adresse(ligne1, ligne2, codePostal, ville, pays, codePays);
    }
}
