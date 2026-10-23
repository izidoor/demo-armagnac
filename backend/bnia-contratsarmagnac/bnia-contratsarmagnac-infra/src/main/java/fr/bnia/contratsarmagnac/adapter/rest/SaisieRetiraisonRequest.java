package fr.bnia.contratsarmagnac.adapter.rest;

import fr.bnia.contratsarmagnac.application.ExecuterContrats.SaisieRetiraison;
import fr.bnia.contratsarmagnac.application.ExecuterContrats.SaisieRetiraison.SaisieLigneRetiraison;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "SaisieRetiraison", description = "Une retiraison : date d'enlèvement, dernière ou non, lignes du contrat retirées (spec 06 §3.4, RG-CA-13)")
public record SaisieRetiraisonRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Au plus tôt aujourd'hui (H-CA-3)")
        LocalDate dateEnlevement,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Sa validation exécute le contrat (RG-CA-15)")
        boolean derniere,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<LigneRequest> lignes) {

    @Schema(name = "SaisieLigneRetiraison")
    public record LigneRequest(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Rang de la ligne du contrat, à partir de 0")
            int rang,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Hectolitres, au plus le reste à retirer")
            BigDecimal volume) {
    }

    SaisieRetiraison versSaisie() {
        return new SaisieRetiraison(dateEnlevement, derniere, lignes == null ? List.of()
                : lignes.stream().map(l -> new SaisieLigneRetiraison(l.rang(), l.volume())).toList());
    }
}
