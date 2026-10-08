package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.SaisieDroits;
import fr.bnia.operateurs.domain.partage.ModulePortail;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(name = "SaisieDroits", description = "Droits d'un contact au portail (C.1.9.2)")
public record SaisieDroitsRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Exige un e-mail (RG-OPE-29)") boolean acces,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Exige l'accès ; donne tous les modules (RG-OPE-30)")
        boolean administrateur,
        @Schema(description = "Ignorés sans accès (RG-OPE-31)") Set<ModulePortail> modules) {

    SaisieDroits versSaisie() {
        return new SaisieDroits(acces, administrateur, modules);
    }
}
