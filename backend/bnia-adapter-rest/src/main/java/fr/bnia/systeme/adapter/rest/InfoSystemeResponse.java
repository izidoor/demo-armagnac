package fr.bnia.systeme.adapter.rest;

import fr.bnia.systeme.application.InfoSysteme;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "InfoSysteme", description = "Identification de l'application et de la base qui la sert")
public record InfoSystemeResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "BNIA") String application,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "0.1.0-SNAPSHOT") String version,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "PostgreSQL 18.0") String versionBase) {

    static InfoSystemeResponse depuis(InfoSysteme info) {
        return new InfoSystemeResponse(info.application(), info.version(), info.versionBase());
    }
}
