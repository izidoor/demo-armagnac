package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.partage.TypeOperateur;

/**
 * Filtres du listing du back-office (§4.3.1 p.18), plus l'état, et si la fiche est facturable ou certifiable.
 * Un filtre {@code null} ou vide ne filtre pas.
 *
 * @param recherche texte cherché, sans distinction de casse, dans le code, la raison sociale, le nom
 *     commercial, le code postal ou la ville (une seule de ces colonnes suffit ; décision du 2026-10-12)
 * @param facturable  vrai : seulement les fiches facturables (RG-OPE-02) ; faux : seulement les autres
 * @param certifiable vrai : seulement les fiches certifiables (RG-OPE-02) ; faux : seulement les autres
 */
public record FiltreOperateurs(
        TypeOperateur type,
        String recherche,
        Boolean actif,
        Boolean facturable,
        Boolean certifiable) {

    public static FiltreOperateurs aucun() {
        return new FiltreOperateurs(null, null, null, null, null);
    }
}
