package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.domain.Contact;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ContactLigne", description = "Ligne de l'onglet Contacts")
public record ContactLigneResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Jeanne Durand") String nomComplet,
        String fonction,
        String email,
        String telephone,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean acces,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean administrateur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean actif) {

    static ContactLigneResponse depuis(Contact c) {
        var i = c.identite();
        return new ContactLigneResponse(c.id(), i.nomComplet(), i.fonction(), i.email() == null ? null : i.email().valeur(),
                i.telephoneProfessionnel(), c.droits().acces(), c.droits().administrateur(), c.estActif());
    }
}
