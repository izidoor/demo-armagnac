package fr.bnia.referentiels.adapter.rest;

import fr.bnia.referentiels.application.SaisieTauxTva;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "SaisieTauxTva")
public record SaisieTauxTvaRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Pourcentage entre 0 et 100, deux décimales au plus", example = "20") BigDecimal taux,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "2014-01-01") LocalDate dateDebut,
        @Schema(description = "Facultative : sans fin, le taux vaut pour toutes les dates postérieures") LocalDate dateFin) {

    SaisieTauxTva versSaisie() {
        return new SaisieTauxTva(taux, dateDebut, dateFin);
    }
}
