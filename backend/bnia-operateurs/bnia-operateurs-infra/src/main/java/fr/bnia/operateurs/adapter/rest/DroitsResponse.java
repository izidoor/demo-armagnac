package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.Module;
import fr.bnia.operateurs.domain.partage.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Droits", description = "Droits de l'utilisateur connecté sur un opérateur, évalués à l'appel (RG-OPE-31, 32)")
public record DroitsResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long operateur,
        @Schema(description = "Rôle de son rattachement actif dans cet opérateur") Role role,
        @Schema(description = "Rôle tenu dans l'opérateur BNIA, valable pour tous les opérateurs") Role roleBnia,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Niveau contacts,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Niveau acces,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Niveau parametrage,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Champs réservés, listing des opérateurs (RG-OPE-32, 36)")
        boolean personnelBnia,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Fin d'un opérateur, blocage (RG-OPE-25, 26, 50)")
        boolean adminBnia) {

    /** Ce qu'un module permet. */
    @Schema(name = "NiveauDroit")
    public enum Niveau {
        AUCUN, LECTURE, GESTION
    }

    static DroitsResponse depuis(Droits d) {
        return new DroitsResponse(d.operateur(), d.local().orElse(null), d.bnia().orElse(null), niveau(d, Module.CONTACTS),
                niveau(d, Module.ACCES), niveau(d, Module.PARAMETRAGE), d.personnelBnia(), d.adminBnia());
    }

    private static Niveau niveau(Droits d, Module module) {
        return d.gere(module) ? Niveau.GESTION : d.lit(module) ? Niveau.LECTURE : Niveau.AUCUN;
    }
}
