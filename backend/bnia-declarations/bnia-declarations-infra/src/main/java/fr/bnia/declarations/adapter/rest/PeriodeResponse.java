package fr.bnia.declarations.adapter.rest;

import fr.bnia.declarations.domain.PeriodeDeclaration;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(name = "PeriodeDeclaration", description = "Période d'une DSM : année du 1er avril au 31 mars, ou mois civil (RG-DEC-03)")
public record PeriodeResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate debut,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate fin,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "juin 2026") String libelle) {

    static PeriodeResponse depuis(PeriodeDeclaration p) {
        return new PeriodeResponse(p.debut(), p.fin(), p.libelle());
    }
}
