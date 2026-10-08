package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.LigneJournal;
import fr.bnia.operateurs.domain.ActionJournal;
import fr.bnia.operateurs.domain.TypeCible;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(name = "LigneJournal", description = "Action sur une personne ou un accès (RG-OPE-34)")
public record LigneJournalResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant date,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Copie du nom de l'acteur", example = "Anne Lamarque")
        String acteur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) ActionJournal action,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Invitation envoyée") String libelle,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) TypeCible cible,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long idCible,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Copie du nom de la cible") String nomCible,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<ChangementResponse> changements) {

    @Schema(name = "ChangementJournal")
    public record ChangementResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "téléphone professionnel") String champ,
            String avant,
            String apres) {
    }

    static LigneJournalResponse depuis(LigneJournal l) {
        return new LigneJournalResponse(l.date(), l.nomActeur(), l.action(), l.action().libelle(), l.cible(),
                l.idCible(), l.nomCible(), l.changements().stream()
                .map(c -> new ChangementResponse(c.champ().libelle(), c.avant(), c.apres())).toList());
    }
}
