package fr.bnia.declarations.adapter.rest;

import fr.bnia.declarations.domain.NoteInterne;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "NoteInterneDsm", description = "Note interne du BNIA sur une DSM, jamais montrée à l'opérateur (spec 15 §3.7)")
public record NoteInterneResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
        @Schema(description = "Ligne visée ; vide pour la DSM") Long ligne,
        @Schema(description = "Libellé de la ligne visée, supprimée depuis ; la note porte alors sur la DSM")
        String ligneSupprimee,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Copie du nom de l'auteur") String auteur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant ecriteLe,
        @Schema(description = "Renseigné si modifiée (« modifiée » affiché)") Instant modifieeLe,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String texte,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Écrite par l'utilisateur connecté : il la modifie ou la supprime") boolean deLUtilisateur) {

    static NoteInterneResponse depuis(NoteInterne n, long utilisateur) {
        return new NoteInterneResponse(n.id(), n.ligne(), n.ligneSupprimee(), n.auteur().nom(), n.ecriteLe(),
                n.modifieeLe(), n.texte(), n.auteur().utilisateur() == utilisateur);
    }
}
