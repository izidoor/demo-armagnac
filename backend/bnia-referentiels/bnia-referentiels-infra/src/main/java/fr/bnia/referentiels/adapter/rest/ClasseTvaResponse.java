package fr.bnia.referentiels.adapter.rest;

import fr.bnia.referentiels.domain.ClasseTva;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "ClasseTva", description = "Classe de TVA avec l'historique de ses taux")
public record ClasseTvaResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "1") long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "NORMAL") String code,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Normal") String libelle,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean actif,
        @Schema(description = "Taux applicable aujourd'hui ; absent s'il n'y en a pas (RG-REF-12)") TauxTvaResponse tauxEnVigueur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Du plus récent au plus ancien") List<TauxTvaResponse> taux) {

    static ClasseTvaResponse depuis(ClasseTva classe, LocalDate aujourdhui) {
        return new ClasseTvaResponse(classe.id(), classe.code().valeur(), classe.libelle().valeur(), classe.estActif(),
                classe.tauxApplicable(aujourdhui).map(TauxTvaResponse::depuis).orElse(null),
                classe.taux().stream().map(TauxTvaResponse::depuis).toList());
    }
}
