package fr.bnia.declarations.adapter.rest;

import fr.bnia.declarations.domain.CalendrierDeclaratif.Situation;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SituationDsm", description = "La prochaine DSM de l'opérateur, ou ce qui en empêche l'initiation (RG-DEC-02 à 05)")
public record SituationDsmResponse(
        @Schema(description = "Période à initier, ou de la DSM en cours ; vide sans point de départ") PeriodeResponse periode,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean initiable,
        @Schema(description = "TYPE_NON_DECLARANT, STATUT_NON_DECLARANT, SANS_POINT_DE_DEPART, DSM_EN_COURS, PERIODE_PAS_COMMENCEE")
        String empechement,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Raison, pour l'écran ; vide si rien n'empêche")
        String message,
        @Schema(description = "La DSM non validée, s'il y en a une") Long dsmEnCours) {

    static SituationDsmResponse depuis(Situation s) {
        return new SituationDsmResponse(s.periode().map(PeriodeResponse::depuis).orElse(null), s.initiable(),
                s.empechement().map(Enum::name).orElse(null), s.message(), s.dsmEnCours().orElse(null));
    }
}
