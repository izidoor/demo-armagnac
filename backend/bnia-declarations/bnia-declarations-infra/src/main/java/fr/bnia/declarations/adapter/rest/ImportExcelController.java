package fr.bnia.declarations.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.declarations.application.FichierDepose;
import fr.bnia.declarations.application.GererDsm;
import fr.bnia.declarations.application.GererDsm.DsmImportee;
import fr.bnia.declarations.application.GererPointDeDepart;
import fr.bnia.declarations.application.GererPointDeDepart.PointImporte;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Import du modèle Excel (spec 05 §5.1, H-DEC-15), tout ou rien : dans une DSM (UC-DEC-5), ou en stock initial
 * d'un point de départ (BNIA). Un fichier refusé rend 422 avec ses erreurs situées ({@link ImportRefuseResponse}) ;
 * le fichier n'est pas conservé.
 */
@RestController
@Tag(name = "declarations")
class ImportExcelController {

    private final GererDsm dsms;
    private final GererPointDeDepart points;

    ImportExcelController(GererDsm dsms, GererPointDeDepart points) {
        this.dsms = dsms;
        this.points = points;
    }

    @Schema(name = "DsmImportee", description = "La DSM après l'import, et ses avertissements (RG-DEC-38)")
    record DsmImporteeResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) DsmResponse dsm,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<String> avertissements) {
    }

    @Schema(name = "PointDeDepartImporte", description = "Le point de départ après l'import, et ses avertissements (RG-DEC-38)")
    record PointImporteResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) PointDeDepartResponse point,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<String> avertissements) {
    }

    @PostMapping(path = "/api/operateurs/{operateur}/dsm/{id}/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(operationId = "importerDsm",
            summary = "L'opérateur importe le modèle Excel dans sa DSM modifiable : colonnes 2 à 25 remplacées, lignes"
                    + " rapprochées ; tout ou rien (RG-DEC-21, 37, 38)",
            responses = {@ApiResponse(responseCode = "200", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "422", description = "Fichier refusé, avec ses erreurs",
                            content = @Content(mediaType = "application/problem+json",
                                    schema = @Schema(implementation = ImportRefuseResponse.class)))})
    DsmImporteeResponse importerDsm(Connecte connecte, @PathVariable long operateur, @PathVariable long id,
            @RequestPart("fichier") MultipartFile fichier, @RequestParam(required = false) Long version) {
        DsmImportee importee = dsms.importer(Acteurs.de(connecte), operateur, id, fichier(fichier), version);
        return new DsmImporteeResponse(DsmResponse.depuis(importee.vue()), importee.avertissements());
    }

    @PostMapping(path = "/api/operateurs/{operateur}/point-de-depart/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(operationId = "importerPointDeDepart",
            summary = "Le BNIA importe le modèle Excel en stock initial : le stock de fin du fichier, au lendemain de sa"
                    + " période ou à la date choisie ; fixe ou modifie le point de départ ; tout ou rien (RG-DEC-08, 37, 38)",
            responses = {@ApiResponse(responseCode = "200", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "422", description = "Fichier refusé, avec ses erreurs",
                            content = @Content(mediaType = "application/problem+json",
                                    schema = @Schema(implementation = ImportRefuseResponse.class)))})
    PointImporteResponse importerPointDeDepart(Connecte connecte, @PathVariable long operateur,
            @RequestPart("fichier") MultipartFile fichier,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false) Long version) {
        PointImporte importe = points.importer(Acteurs.de(connecte), operateur, fichier(fichier),
                Optional.ofNullable(debut), version);
        return new PointImporteResponse(PointDeDepartResponse.depuis(importe.point()), importe.avertissements());
    }

    private static FichierDepose fichier(MultipartFile fichier) {
        try {
            return new FichierDepose(fichier.getOriginalFilename(), fichier.getBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
