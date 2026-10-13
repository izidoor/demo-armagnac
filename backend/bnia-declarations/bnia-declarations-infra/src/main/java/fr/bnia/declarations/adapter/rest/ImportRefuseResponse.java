package fr.bnia.declarations.adapter.rest;

import fr.bnia.declarations.domain.ImportRefuse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Le corps d'un import refusé (422, RFC 9457) : le problème, la règle et les erreurs situées (RG-DEC-37). */
@Schema(name = "ImportRefuse", description = "Import Excel refusé, tout ou rien : chaque erreur à sa place dans le classeur (RG-DEC-37)")
public record ImportRefuseResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String title,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int status,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String detail,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "RG-DEC-37") String regle,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<ErreurImportResponse> erreurs) {

    @Schema(name = "ErreurImport", description = "Une erreur du fichier importé, située")
    public record ErreurImportResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Ventes France") String onglet,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Numéro de ligne du classeur") int ligne,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Lettre de colonne du classeur",
                    example = "D") String colonne,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String message) {

        static ErreurImportResponse depuis(ImportRefuse.Erreur erreur) {
            return new ErreurImportResponse(erreur.onglet().libelle(), erreur.ligne(), erreur.colonne(), erreur.message());
        }
    }
}
