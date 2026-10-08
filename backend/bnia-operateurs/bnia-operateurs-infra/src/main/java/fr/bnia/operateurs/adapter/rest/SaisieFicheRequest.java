package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.SaisieFiche;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(name = "SaisieFiche", description = "Modification d'une fiche professionnelle (RG-OPE-46)")
public record SaisieFicheRequest(
        @Schema(example = "Maître de chai") String fonction,
        String telephoneProfessionnel,
        String telephoneSecondaire,
        String adresse,
        String codePostal,
        String ville,
        @Schema(description = "Identifiant du pays") Long pays,
        @Schema(description = "Identifiants des centres d'intérêt") Set<Long> centresInteret,
        String observation,
        @Schema(description = "Version lue (RG-OPE-51)") Long version) {

    SaisieFiche versSaisie() {
        return new SaisieFiche(fonction, telephoneProfessionnel, telephoneSecondaire, adresse, codePostal, ville, pays,
                centresInteret, observation, version);
    }
}
