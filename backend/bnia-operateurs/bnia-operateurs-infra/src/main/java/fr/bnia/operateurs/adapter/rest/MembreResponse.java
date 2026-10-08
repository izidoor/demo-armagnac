package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.FicheMembre;
import fr.bnia.operateurs.domain.Rattachement;
import fr.bnia.operateurs.domain.StatutRattachement;
import fr.bnia.operateurs.domain.Utilisateur;
import fr.bnia.operateurs.domain.partage.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "Membre", description = "Un membre d'un opérateur : son rattachement, son identité lue chez l'utilisateur, sa fiche")
public record MembreResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Identifiant du rattachement") long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long operateur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long utilisateur,
        @Schema(description = "Identifiant du titre ; modifié par la personne seule (RG-OPE-47)") Long titre,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nom,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String prenom,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nomComplet,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String email,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Role role,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) StatutRattachement statut,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Bloqué par le BNIA (RG-OPE-50)") boolean bloqueParBnia,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant depuis,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) FicheResponse fiche,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean contactPrincipal,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean destinataireFactures,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Dernier ADMIN actif de l'opérateur (RG-OPE-35)")
        boolean dernierAdmin) {

    static MembreResponse depuis(FicheMembre m) {
        Rattachement r = m.rattachement();
        Utilisateur u = m.utilisateur();
        return new MembreResponse(r.id(), r.operateur(), u.id(), u.identite().titre(), u.identite().nom(),
                u.identite().prenom(), u.identite().nomComplet(), u.email().valeur(), r.role(), r.statut(),
                u.estBloque(), r.creeLe(), FicheResponse.depuis(m.fiche()), m.contactPrincipal(),
                m.destinataireFactures(), m.dernierAdmin());
    }
}
