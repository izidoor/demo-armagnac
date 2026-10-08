package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.SaisieDiffusion;
import fr.bnia.operateurs.domain.partage.NiveauDiffusion;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "SaisieDiffusion", description = "Onglet Diffusion : millésimes diffusables, accès aux statistiques du portail")
public record SaisieDiffusionRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) NiveauDiffusion niveau,
        @Schema(description = "En accord partiel : au moins un, sans doublon ; ignorés sinon (RG-OPE-14)", example = "[2005, 2010]")
        List<Integer> millesimes,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "RG-OPE-15") boolean accesStatistiques) {

    SaisieDiffusion versSaisie() {
        return new SaisieDiffusion(niveau, millesimes, accesStatistiques);
    }
}
