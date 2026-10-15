package fr.bnia.facturation.adapter.rest;

import fr.bnia.facturation.domain.CoteJournal;
import fr.bnia.facturation.domain.EntreeJournal;
import fr.bnia.facturation.domain.EvenementJournal;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "EntreeJournalPiece", description = "Entrée du journal d'une pièce (spec 08 §3.9, RG-FAC-20)")
public record EntreeJournalPieceResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant date,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) EvenementJournal evenement,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Émise") String libelle,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Copie du nom de l'utilisateur, ou nom du traitement")
        String acteur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) CoteJournal cote,
        String detail) {

    static EntreeJournalPieceResponse de(EntreeJournal e) {
        return new EntreeJournalPieceResponse(e.date(), e.evenement(), e.evenement().libelle(), e.nomActeur(), e.cote(),
                e.detail());
    }
}
