package fr.bnia.referentiels.adapter.rest;

import fr.bnia.referentiels.application.ClasseTvaLigne;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "ClasseTvaLigne", description = "Ligne du listing des classes de TVA")
public record ClasseTvaLigneResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "1") long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "NORMAL") String code,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Normal") String libelle,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean actif,
        @Schema(description = "Taux applicable aujourd'hui ; absent s'il n'y en a pas (RG-REF-12)", example = "20.00") BigDecimal tauxEnVigueur,
        @Schema(description = "Date d'entrée en vigueur de ce taux ; absente s'il n'y en a pas", example = "2014-01-01") LocalDate debutTauxEnVigueur) {

    static ClasseTvaLigneResponse depuis(ClasseTvaLigne ligne) {
        return new ClasseTvaLigneResponse(ligne.id(), ligne.code(), ligne.libelle(), ligne.actif(), ligne.tauxEnVigueur(),
                ligne.debutTauxEnVigueur());
    }
}
