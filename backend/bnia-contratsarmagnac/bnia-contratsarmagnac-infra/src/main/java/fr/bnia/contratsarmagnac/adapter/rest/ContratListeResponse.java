package fr.bnia.contratsarmagnac.adapter.rest;

import fr.bnia.contratsarmagnac.application.ConsulterContrats.ContratListe;
import fr.bnia.contratsarmagnac.application.ConsulterContrats.Onglet;
import fr.bnia.contratsarmagnac.application.ContratLigne;
import fr.bnia.contratsarmagnac.domain.EtatContrat;
import fr.bnia.contratsarmagnac.domain.Partie;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Schema(name = "ContratListe", description = "Un contrat dans une liste, sur sa version courante (RG-CA-25)")
public record ContratListeResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "CA-2026-0001") String numero,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) EtatContrat etat,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) PartieResponse acheteur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) PartieResponse vendeur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Partie ouvertPar,
        @Schema(description = "En projet : la partie dont l'approbation manque") Partie attend,
        @Schema(description = "Au portail : l'onglet où il se range pour l'opérateur actif") Onglet onglet,
        @Schema(description = "Date de la conclusion") LocalDate dateContrat,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Date de la version courante") Instant modifieLe,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int versionCourante,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int nombreLignes,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "La première ligne : « 2015 » ou « Hors d'âge 20+ »")
        String produit,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Litres d'alcool pur") BigDecimal volumeAp,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "€ HT") BigDecimal montantHt,
        LocalDate aRetirerAvantLe) {

    static ContratListeResponse depuis(ContratListe c) {
        ContratLigne l = c.ligne();
        return new ContratListeResponse(l.id(), l.numero(), l.etat(), PartieResponse.depuis(c.acheteur()),
                PartieResponse.depuis(c.vendeur()), l.ouvertPar(), l.attend().orElse(null), c.onglet().orElse(null),
                l.dateContrat().orElse(null), l.modifieLe(), l.versionCourante(), l.nombreLignes(), l.produit(),
                l.volumeAp().litres(), l.montantHt(), l.aRetirerAvantLe().orElse(null));
    }
}
