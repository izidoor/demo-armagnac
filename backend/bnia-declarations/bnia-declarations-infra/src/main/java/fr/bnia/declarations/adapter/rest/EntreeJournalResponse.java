package fr.bnia.declarations.adapter.rest;

import fr.bnia.declarations.application.EntreeJournal;
import fr.bnia.declarations.domain.ActionJournalDeclarations;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "EntreeJournalDeclarations", description = "Action sur une DSM ou le point de départ, sans valeurs avant et après (RG-DEC-23)")
public record EntreeJournalResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant date,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Copie du nom de l'acteur") String acteur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) ActionJournalDeclarations action,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "DSM enregistrée") String libelle,
        @Schema(description = "DSM concernée ; vide pour le point de départ") Long dsm,
        @Schema(description = "Copie de la période de la DSM", example = "juin 2026") String periode,
        @Schema(description = "Nom de fichier, mois, format…") String detail) {

    static EntreeJournalResponse depuis(EntreeJournal e) {
        return new EntreeJournalResponse(e.date(), e.nomActeur(), e.action(), e.action().libelle(), e.dsm(), e.periode(),
                e.detail());
    }
}
