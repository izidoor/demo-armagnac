package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.Doublon;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Doublon", description = "Valeur déjà portée par une autre fiche : avertissement non bloquant (RG-OPE-06, 08)")
public record DoublonResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Doublon.Champ champ,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "RG-OPE-06") String regle,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) OperateurResumeResponse operateur) {

    static DoublonResponse depuis(Doublon d) {
        return new DoublonResponse(d.champ(), d.regle(), OperateurResumeResponse.depuis(d.operateur()));
    }
}
