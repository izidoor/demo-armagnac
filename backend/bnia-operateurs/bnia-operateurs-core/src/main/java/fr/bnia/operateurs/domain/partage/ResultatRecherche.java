package fr.bnia.operateurs.domain.partage;

import java.util.Optional;

/**
 * Résultat de la recherche restreinte du portail (RG-OPE-37) : code, raison sociale, nom commercial et
 * ville, rien d'autre. Sert aussi d'identité d'opérateur aux listings des autres contextes
 * ({@code OperateursLecture.identites}).
 */
public record ResultatRecherche(long id, String code, String raisonSociale, Optional<String> nomCommercial,
        Optional<String> ville) {
}
