package fr.bnia.declarations.adapter.rest;

import fr.bnia.declarations.application.DsmLigne;
import fr.bnia.declarations.application.ListerDsm.DsmListee;
import fr.bnia.declarations.domain.TypeDeclarant;
import fr.bnia.declarations.domain.partage.Cote;
import fr.bnia.declarations.domain.partage.EtatDsm;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

@Schema(name = "DsmListee",
        description = "Une ligne du listing du BNIA : une DSM, ou la période attendue d'un opérateur sans DSM (absente), avec son opérateur (spec 05 §6.1, UC-DEC-7, RG-DEC-40)")
public record DsmListeeResponse(
        @Schema(description = "Vide pour une période absente") Long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long operateur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "N320001") String codeOperateur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String raisonSociale,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) TypeDeclarant type,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) PeriodeResponse periode,
        @Schema(description = "Vide pour une période absente") EtatDsm etat,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "À prendre en charge",
                description = "Du point de vue du BNIA ; « Absente » sans DSM") String libelleEtat,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Période attendue sans DSM initiée (RG-DEC-40)") boolean absente,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Échéance de la période (RG-DEC-39)")
        LocalDate echeance,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Échéance passée, DSM absente, en brouillon ou en compléments demandés (RG-DEC-40)")
        boolean enRetard,
        Instant envoyeeLe,
        Instant valideeLe,
        @Schema(description = "Date de la dernière action sur la DSM, lue au journal") Instant modifieeLe,
        @Schema(description = "Facturé (factures moins avoirs) ou calculé ; absent si non calculable (RG-DEC-32, 34) ou sans DSM",
                example = "1855.95") BigDecimal totalHt,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "A au moins une facture (RG-DEC-34)")
        boolean facturee,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "L'utilisateur connecté peut la prendre en instruction depuis le listing (RG-DEC-20)")
        boolean prendreEnInstruction) {

    static DsmListeeResponse depuis(DsmListee listee) {
        Optional<DsmLigne> dsm = listee.dsm();
        return new DsmListeeResponse(dsm.map(DsmLigne::id).orElse(null), listee.operateur(), listee.code(),
                listee.raisonSociale(), listee.type(), PeriodeResponse.depuis(listee.periode()),
                dsm.map(DsmLigne::etat).orElse(null),
                dsm.map(d -> d.etat().libelle(Cote.BNIA)).orElse("Absente"), listee.absente(), listee.echeance(),
                listee.enRetard(), dsm.map(DsmLigne::envoyeeLe).orElse(null), dsm.map(DsmLigne::valideeLe).orElse(null),
                dsm.map(DsmLigne::modifieeLe).orElse(null), listee.totalHt().orElse(null),
                dsm.map(DsmLigne::facturee).orElse(false), listee.prendreEnInstruction());
    }
}
