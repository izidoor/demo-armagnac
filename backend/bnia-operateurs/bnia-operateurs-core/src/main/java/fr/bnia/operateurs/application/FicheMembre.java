package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.FicheProfessionnelle;
import fr.bnia.operateurs.domain.Rattachement;
import fr.bnia.operateurs.domain.Utilisateur;

/**
 * Un membre d'un opérateur : son rattachement, son identité lue chez l'utilisateur (vision principe 4),
 * sa fiche professionnelle, et ce que l'écran en montre avant d'agir (désignations, dernier ADMIN).
 *
 * @param dernierAdmin vrai s'il est le dernier ADMIN actif de l'opérateur (RG-OPE-35)
 */
public record FicheMembre(Rattachement rattachement, Utilisateur utilisateur, FicheProfessionnelle fiche,
        boolean contactPrincipal, boolean destinataireFactures, boolean dernierAdmin) {
}
