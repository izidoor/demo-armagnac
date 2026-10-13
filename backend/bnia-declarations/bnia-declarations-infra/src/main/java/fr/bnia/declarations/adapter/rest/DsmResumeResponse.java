package fr.bnia.declarations.adapter.rest;

import fr.bnia.declarations.application.DsmLigne;
import fr.bnia.declarations.domain.Cote;
import fr.bnia.declarations.domain.EtatDsm;
import fr.bnia.declarations.domain.TypeDeclarant;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "DsmResume", description = "Une DSM dans un listing (spec 05 §6.1, §6.2)")
public record DsmResumeResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) TypeDeclarant type,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) PeriodeResponse periode,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) EtatDsm etat,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Envoyée",
                description = "Du point de vue de l'utilisateur connecté") String libelleEtat,
        Instant envoyeeLe,
        Instant valideeLe,
        @Schema(description = "Date de la dernière action sur la DSM, lue au journal") Instant modifieeLe) {

    static DsmResumeResponse depuis(DsmLigne l, Cote cote) {
        return new DsmResumeResponse(l.id(), l.type(), PeriodeResponse.depuis(l.periode()), l.etat(), l.etat().libelle(cote),
                l.envoyeeLe(), l.valideeLe(), l.modifieeLe());
    }
}
