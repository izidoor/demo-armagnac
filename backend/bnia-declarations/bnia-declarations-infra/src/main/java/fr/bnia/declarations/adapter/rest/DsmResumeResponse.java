package fr.bnia.declarations.adapter.rest;

import fr.bnia.declarations.application.DsmLigne;
import fr.bnia.declarations.application.GererDsm.DsmDeLOperateur;
import fr.bnia.declarations.domain.TypeDeclarant;
import fr.bnia.declarations.domain.partage.Cote;
import fr.bnia.declarations.domain.partage.EtatDsm;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

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
        @Schema(description = "Date de la dernière action sur la DSM, lue au journal") Instant modifieeLe,
        @Schema(description = "Facturé (factures moins avoirs) ou calculé ; absent si non calculable (RG-DEC-32, 34)",
                example = "1855.95") BigDecimal totalHt,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "A au moins une facture (RG-DEC-34)")
        boolean facturee,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Échéance de la période (RG-DEC-39)")
        LocalDate echeance) {

    static DsmResumeResponse depuis(DsmDeLOperateur d, Cote cote) {
        DsmLigne l = d.dsm();
        return new DsmResumeResponse(l.id(), l.type(), PeriodeResponse.depuis(l.periode()), l.etat(), l.etat().libelle(cote),
                l.envoyeeLe(), l.valideeLe(), l.modifieeLe(), d.totalHt().orElse(null), l.facturee(),
                l.periode().echeance(l.type()));
    }
}
