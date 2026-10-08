package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.partage.TypeOperateur;

/**
 * Filtres du listing du back-office (§4.3.1 p.18), plus l'état et la complétude. Un filtre {@code null}
 * ou vide ne filtre pas. Les textes sont cherchés sans distinction de casse ; le code postal par son
 * début.
 *
 * @param incomplet vrai : seulement les fiches incomplètes (RG-OPE-02) ; faux : seulement les complètes
 */
public record FiltreOperateurs(
        TypeOperateur type,
        String code,
        String raisonSociale,
        String nomCommercial,
        String codePostal,
        String ville,
        Boolean actif,
        Boolean incomplet) {

    public static FiltreOperateurs aucun() {
        return new FiltreOperateurs(null, null, null, null, null, null, null, null);
    }
}
