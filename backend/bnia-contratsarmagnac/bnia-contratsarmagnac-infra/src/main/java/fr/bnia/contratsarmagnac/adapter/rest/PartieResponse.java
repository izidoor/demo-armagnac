package fr.bnia.contratsarmagnac.adapter.rest;

import fr.bnia.operateurs.domain.partage.OperateurVue;
import fr.bnia.operateurs.domain.partage.ResultatRecherche;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PartieContrat", description = "Un opérateur partie au contrat : son identité")
public record PartieResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String code,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String raisonSociale,
        String nomCommercial,
        String ville) {

    static PartieResponse depuis(ResultatRecherche r) {
        return new PartieResponse(r.id(), r.code(), r.raisonSociale(), r.nomCommercial().orElse(null),
                r.ville().orElse(null));
    }

    static PartieResponse depuis(OperateurVue o) {
        return new PartieResponse(o.id(), o.code(), o.raisonSociale(), o.nomCommercial().orElse(null),
                o.adresse().ville().orElse(null));
    }
}
