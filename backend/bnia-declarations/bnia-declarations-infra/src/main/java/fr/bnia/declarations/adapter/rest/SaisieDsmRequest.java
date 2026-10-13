package fr.bnia.declarations.adapter.rest;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.CleConditionnement;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.SaisieDsm;
import fr.bnia.declarations.domain.SaisieLigneDsm;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Schema(name = "SaisieDsm", description = "Toute la saisie d'une DSM : lignes, volumes, date de DIA (RG-DEC-10 à 19)")
public record SaisieDsmRequest(
        @Schema(description = "Facultative (RG-DEC-19)") LocalDate dateDia,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Toutes les lignes, dans l'ordre de l'écran")
        List<SaisieLigneDsmRequest> lignes,
        @Schema(description = "Version lue, à l'enregistrement (verrou optimiste)") Long version) {

    @Schema(name = "SaisieLigneDsm")
    public record SaisieLigneDsmRequest(
            @Schema(description = "Ligne existante ; vide pour une ligne ajoutée") Long id,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) CleConditionnement cle,
            @Schema(description = "Lignes MB et MV") Integer millesime,
            @Schema(description = "Lignes NB et NV") String denomination,
            @Schema(description = "Ignoré pour une ligne MB, dont le compte est calculé", example = "12") String compte,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                    description = "26 valeurs en litres d'alcool pur, indice 0 = colonne 1 ; colonnes 1 et 26 vides (calculées)")
            List<BigDecimal> volumes) {
    }

    SaisieDsm versSaisie() {
        return new SaisieDsm(dateDia, lignes == null ? List.of() : lignes.stream().map(l -> new SaisieLigneDsm(l.id(),
                l.cle(), l.millesime(), l.denomination(), l.compte(), volumes(l.volumes()))).toList());
    }

    private static Map<ColonneDsm, VolumeAlcoolPur> volumes(List<BigDecimal> volumes) {
        Map<ColonneDsm, VolumeAlcoolPur> map = new EnumMap<>(ColonneDsm.class);
        if (volumes == null) {
            return map;
        }
        for (int i = 0; i < Math.min(volumes.size(), 26); i++) {
            if (volumes.get(i) != null) {
                map.put(ColonneDsm.numero(i + 1), VolumeAlcoolPur.litres(volumes.get(i)));
            }
        }
        return map;
    }
}
