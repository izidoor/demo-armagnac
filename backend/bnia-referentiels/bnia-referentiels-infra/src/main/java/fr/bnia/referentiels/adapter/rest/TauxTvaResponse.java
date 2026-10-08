package fr.bnia.referentiels.adapter.rest;

import fr.bnia.referentiels.domain.TauxTva;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "TauxTva")
public record TauxTvaResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Pourcentage", example = "20.00") BigDecimal taux,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "2014-01-01") LocalDate dateDebut,
        @Schema(description = "Absente : taux sans terme") LocalDate dateFin) {

    static TauxTvaResponse depuis(TauxTva taux) {
        return new TauxTvaResponse(taux.taux(), taux.debut(), taux.fin());
    }
}
