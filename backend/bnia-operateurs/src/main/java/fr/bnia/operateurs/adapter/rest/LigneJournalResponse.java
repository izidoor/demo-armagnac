package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.LigneJournal;
import fr.bnia.operateurs.domain.ActionContact;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(name = "LigneJournalContact", description = "Entrée du journal d'un contact (RG-OPE-34)")
public record LigneJournalResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant date,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "BNIA") String auteur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nomContact,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) ActionContact action,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<ChangementResponse> changements) {

    @Schema(name = "ChangementContact")
    public record ChangementResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "téléphone professionnel") String champ,
            String avant,
            String apres) {
    }

    static LigneJournalResponse depuis(LigneJournal l) {
        return new LigneJournalResponse(l.date(), l.auteur(), l.nomContact(), l.action(), l.changements().stream()
                .map(c -> new ChangementResponse(c.champ().libelle(), c.avant(), c.apres())).toList());
    }
}
