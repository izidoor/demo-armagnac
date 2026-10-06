package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.domain.partage.NiveauDiffusion;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "SaisieDiffusionPortail", description = "Diffusion des millésimes, au portail ; l'accès aux statistiques reste fixé par le BNIA (RG-OPE-36)")
public record SaisieDiffusionPortailRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) NiveauDiffusion niveau,
        @Schema(description = "En accord partiel : au moins un, sans doublon (RG-OPE-14)") List<Integer> millesimes) {
}
