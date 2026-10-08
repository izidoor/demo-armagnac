package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.UtilisateurLigne;
import fr.bnia.operateurs.domain.StatutRattachement;
import fr.bnia.operateurs.domain.StatutUtilisateur;
import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "UtilisateurLigne", description = "Un utilisateur et ses rattachements (RG-OPE-53)")
public record UtilisateurResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nom,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String prenom,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String email,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) StatutUtilisateur statut,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<RattachementResponse> rattachements) {

    @Schema(name = "RattachementUtilisateur")
    public record RattachementResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long operateur,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String code,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String raisonSociale,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "BNIA : back-office ; autre : portail (RG-OPE-49)")
            TypeOperateur type,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Role role,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) StatutRattachement statut,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Faux : opérateur désactivé, rattachement gelé (RG-OPE-26)")
            boolean operateurEnActivite,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Rattachement actif dans un opérateur actif")
            boolean utilisable) {

        static RattachementResponse depuis(UtilisateurLigne.RattachementLigne r) {
            return new RattachementResponse(r.id(), r.operateur(), r.code(), r.raisonSociale(), r.type(), r.role(),
                    r.statut(), r.operateurEnActivite(), r.statut() == StatutRattachement.ACTIF && r.operateurEnActivite());
        }
    }

    static UtilisateurResponse depuis(UtilisateurLigne u) {
        return new UtilisateurResponse(u.id(), u.nom(), u.prenom(), u.email(), u.statut(),
                u.rattachements().stream().map(RattachementResponse::depuis).toList());
    }
}
