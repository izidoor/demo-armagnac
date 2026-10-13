package fr.bnia.declarations.adapter.rest;

import fr.bnia.declarations.application.DsmLigne;
import fr.bnia.declarations.application.ListerDsm.DsmListee;
import fr.bnia.declarations.domain.Cote;
import fr.bnia.declarations.domain.EtatDsm;
import fr.bnia.declarations.domain.TypeDeclarant;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "DsmListee", description = "Une DSM du listing du BNIA, avec son opérateur (spec 05 §6.1, UC-DEC-7)")
public record DsmListeeResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long operateur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "N320001") String codeOperateur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String raisonSociale,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) TypeDeclarant type,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) PeriodeResponse periode,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) EtatDsm etat,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "À prendre en charge",
                description = "Du point de vue du BNIA") String libelleEtat,
        Instant envoyeeLe,
        Instant valideeLe,
        @Schema(description = "Date de la dernière action sur la DSM, lue au journal") Instant modifieeLe,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "L'utilisateur connecté peut la prendre en instruction depuis le listing (RG-DEC-20)")
        boolean prendreEnInstruction) {

    static DsmListeeResponse depuis(DsmListee listee) {
        DsmLigne l = listee.dsm();
        return new DsmListeeResponse(l.id(), l.operateur(), listee.code(), listee.raisonSociale(), l.type(),
                PeriodeResponse.depuis(l.periode()), l.etat(), l.etat().libelle(Cote.BNIA), l.envoyeeLe(), l.valideeLe(), l.modifieeLe(), listee.prendreEnInstruction());
    }
}
