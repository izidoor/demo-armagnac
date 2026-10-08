package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.domain.CoordonneesProfessionnelles;
import fr.bnia.operateurs.domain.FicheProfessionnelle;
import fr.bnia.operateurs.domain.TypeFiche;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(name = "FicheProfessionnelle", description = "Informations d'une personne dans un opérateur, propriété de l'opérateur (spec 02 §3.2.3)")
public record FicheResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long operateur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) TypeFiche type,
        @Schema(example = "Maître de chai") String fonction,
        String telephoneProfessionnel,
        String telephoneSecondaire,
        String adresse,
        String codePostal,
        String ville,
        @Schema(description = "Identifiant du pays") Long pays,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Identifiants des centres d'intérêt")
        List<Long> centresInteret,
        String observation,
        @Schema(description = "Copie du nom de l'auteur de la dernière modification (RG-OPE-46)") String modifieePar,
        Instant modifieeLe,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "À renvoyer avec chaque modification (RG-OPE-51)")
        long version) {

    static FicheResponse depuis(FicheProfessionnelle f) {
        CoordonneesProfessionnelles c = f.coordonnees();
        return new FicheResponse(f.id(), f.operateur(), f.type(), c.fonction(), c.telephoneProfessionnel(),
                c.telephoneSecondaire(), c.adresse(), c.codePostal(), c.ville(), c.pays(),
                c.centresInteret().stream().sorted().toList(), c.observation(), f.modifieePar(), f.modifieeLe(),
                f.version());
    }
}
