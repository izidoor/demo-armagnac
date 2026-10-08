package fr.bnia.operateurs.adapter.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CodePropose")
public record CodeProposeResponse(
        @Schema(description = "Absent : aucun code ne peut être proposé (négociant sans code postal français)",
                example = "N320004") String code) {
}
