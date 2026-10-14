package fr.bnia.declarations.adapter.rest;

import fr.bnia.declarations.domain.Commentaire;
import fr.bnia.declarations.domain.partage.Cote;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "CommentaireDsm", description = "Commentaire d'instruction d'une DSM (spec 05 §3.2.4)")
public record CommentaireResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
        @Schema(description = "Ligne visée ; vide pour la DSM") Long ligne,
        @Schema(description = "Libellé de la ligne visée, supprimée depuis ; le commentaire porte alors sur la DSM")
        String ligneSupprimee,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Copie du nom de l'auteur") String auteur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Cote cote,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant ecritLe,
        @Schema(description = "Renseigné si modifié (« modifié » affiché)") Instant modifieLe,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String texte,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean interne,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Motif d'une demande de compléments : ne se supprime pas") boolean motif,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Écrit par l'utilisateur connecté : il le modifie ou le supprime") boolean deLUtilisateur) {

    static CommentaireResponse depuis(Commentaire c, long utilisateur) {
        return new CommentaireResponse(c.id(), c.ligne(), c.ligneSupprimee(), c.auteur().nom(), c.cote(), c.ecritLe(),
                c.modifieLe(), c.texte(), c.interne(), c.motif(), c.auteur().utilisateur() == utilisateur);
    }
}
