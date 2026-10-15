package fr.bnia.facturation.adapter.rest;

import fr.bnia.facturation.application.PieceLigne;
import fr.bnia.facturation.domain.EtatPiece;
import fr.bnia.facturation.domain.SensPiece;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Schema(name = "PieceListee", description = "Une pièce de l'historique (RG-FAC-27)")
public record PieceListeeResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) SensPiece sens,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) EtatPiece etat,
        @Schema(description = "Vide pour un brouillon", example = "2026000001") String numero,
        @Schema(description = "Vide pour un brouillon") LocalDate dateEmission,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Armagnacs de Gascogne") String acheteur,
        @Schema(description = "Identifiant du client chez l'hôte (opérateur) ; vide pour un client libre")
        String clientIdentifiant,
        @Schema(description = "N° BNIA du client", example = "N320001") String clientCode,
        @Schema(description = "Nature d'origine ; vide pour un avoir", example = "DSM") String nature,
        @Schema(example = "Cotisation interprofessionnelle Armagnac") String libelleNature,
        @Schema(example = "DSM juillet 2026") String reference,
        @Schema(example = "DSM de juillet 2026") String libellePeriode,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal ht,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal tva,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal ttc,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant creeeLe) {

    static PieceListeeResponse de(PieceLigne p) {
        return new PieceListeeResponse(p.id(), p.sens(), p.etat(), p.numero(), p.dateEmission(), p.nomAcheteur(),
                p.client() == null ? null : p.client().identifiant(), p.client() == null ? null : p.client().code(),
                p.origine() == null ? null : p.origine().nature(),
                p.origine() == null ? null : p.origine().libelleNature(),
                p.origine() == null ? null : p.origine().reference(), p.libellePeriode(), p.ht(), p.tva(), p.ttc(),
                p.creeeLe());
    }
}
