package fr.bnia.contratsarmagnac.adapter.rest;

import fr.bnia.commun.domain.Appellation;
import fr.bnia.contratsarmagnac.application.SaisieContenu;
import fr.bnia.contratsarmagnac.domain.Solde;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "SaisieContenuContrat", description = "Le contenu d'une version du contrat (spec 06 §3.2, RG-CA-03)")
public record SaisieContenuRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<SaisieLigneRequest> lignes,
        @Schema(description = "Caractéristiques complémentaires, imprimées") String caracteristiques,
        @Schema(description = "Conditions et calendrier de retiraison, imprimés") String conditionsRetiraison,
        @Schema(description = "Affichée dans les listes, sans effet sur le stock") LocalDate aRetirerAvantLe,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Solde solde,
        @Schema(description = "Acompte en %, dans ]0 ; 100[") BigDecimal acompte) {

    @Schema(name = "SaisieLigneContrat",
            description = "Une ligne : millésime, ou dénomination et compte d'âge ; volume en hl, degré en % vol., prix en € HT / hl AP")
    public record SaisieLigneRequest(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Appellation appellation,
            Integer millesime,
            String denomination,
            @Schema(description = "Assemblage seulement : 00, 0 … 19, 20+", example = "20+") String compte,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal volume,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal degre,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal prix) {
    }

    SaisieContenu versSaisie() {
        return new SaisieContenu(lignes == null ? List.of() : lignes.stream()
                .map(l -> new SaisieContenu.SaisieLigne(l.appellation(), l.millesime(), l.denomination(), l.compte(),
                        l.volume(), l.degre(), l.prix()))
                .toList(), caracteristiques, conditionsRetiraison, aRetirerAvantLe, solde, acompte);
    }
}
