package fr.bnia.referentiels.adapter.rest;

import fr.bnia.referentiels.application.ContenanceLigne;
import fr.bnia.referentiels.domain.Contenance;
import fr.bnia.referentiels.domain.Libelle;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "Contenance")
public record ContenanceResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "5") long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Volume en litres, unique", example = "0.700") BigDecimal volume,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Libellé saisi, sinon dérivé du volume", example = "0,70 L") String libelle,
        @Schema(description = "Libellé saisi ; absent si dérivé du volume") String libelleSaisi,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean actif) {

    static ContenanceResponse depuis(Contenance contenance) {
        return new ContenanceResponse(contenance.id(), contenance.volume().litres(), contenance.libelle(),
                contenance.libelleSaisi().map(Libelle::valeur).orElse(null), contenance.estActif());
    }

    static ContenanceResponse depuis(ContenanceLigne ligne) {
        return new ContenanceResponse(ligne.id(), ligne.volume(), ligne.libelle(), ligne.libelleSaisi(), ligne.actif());
    }
}
