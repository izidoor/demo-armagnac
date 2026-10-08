package fr.bnia.operateurs.adapter.rest;

import fr.bnia.commun.domain.Appellation;
import fr.bnia.operateurs.application.SaisieProduction;
import fr.bnia.operateurs.domain.TypeAlambic;
import fr.bnia.operateurs.domain.partage.PeriodiciteDsm;
import fr.bnia.operateurs.domain.partage.StatutDeclarationStock;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Schema(name = "SaisieProduction", description = "Onglet Production : déclaratif, région, appellations, alambics")
public record SaisieProductionRequest(
        @Schema(description = "Absente pour un opérateur AUTRE ou BNIA, obligatoire sinon (RG-OPE-01, 10)") PeriodiciteDsm periodicite,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Jamais OUI pour un opérateur AUTRE (RG-OPE-11)")
        StatutDeclarationStock statutDeclarationStock,
        @Schema(description = "Identifiant de la région") Long region,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Set<Appellation> appellations,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<Alambic> alambics,
        @Schema(description = "Version lue (RG-OPE-51)") Long version) {

    @Schema(name = "SaisieAlambic")
    public record Alambic(
            String numeroAgrement,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) TypeAlambic type,
            @Schema(description = "Strictement positive (RG-OPE-13)") BigDecimal capacite) {
    }

    SaisieProduction versSaisie() {
        return new SaisieProduction(periodicite, statutDeclarationStock, region, appellations,
                alambics == null ? List.of() : alambics.stream()
                        .map(a -> new SaisieProduction.Alambic(a.numeroAgrement(), a.type(), a.capacite())).toList(),
                version);
    }
}
