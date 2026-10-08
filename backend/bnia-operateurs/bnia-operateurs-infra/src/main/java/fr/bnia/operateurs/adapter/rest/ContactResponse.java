package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.FicheContact;
import fr.bnia.operateurs.domain.Contact;
import fr.bnia.operateurs.domain.IdentiteContact;
import fr.bnia.operateurs.domain.partage.ModulePortail;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "Contact", description = "Un contact, ses droits au portail et ses désignations")
public record ContactResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
        @Schema(description = "Identifiant du titre") Long titre,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nom,
        String prenom,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nomComplet,
        String fonction,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Identifiants des centres d'intérêt")
        List<Long> centresInteret,
        String telephoneProfessionnel,
        String telephoneSecondaire,
        String email,
        String adresse,
        String codePostal,
        String ville,
        @Schema(description = "Identifiant du pays") Long pays,
        String observation,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean actif,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean acces,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean administrateur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<ModulePortail> modules,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "L'e-mail est l'identifiant d'un compte : seule la personne le change (RG-OPE-32)")
        boolean emailVerrouille,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean contactPrincipal,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean destinataireFactures) {

    static ContactResponse depuis(FicheContact fiche) {
        Contact c = fiche.contact();
        IdentiteContact i = c.identite();
        return new ContactResponse(c.id(), i.titre(), i.nom(), i.prenom(), i.nomComplet(), i.fonction(),
                i.centresInteret().stream().sorted().toList(), i.telephoneProfessionnel(), i.telephoneSecondaire(),
                i.email() == null ? null : i.email().valeur(), i.adresse(), i.codePostal(), i.ville(), i.pays(),
                i.observation(), c.estActif(), c.droits().acces(), c.droits().administrateur(),
                c.droits().modules().stream().sorted().toList(), c.emailVerrouille(), fiche.contactPrincipal(),
                fiche.destinataireFactures());
    }
}
