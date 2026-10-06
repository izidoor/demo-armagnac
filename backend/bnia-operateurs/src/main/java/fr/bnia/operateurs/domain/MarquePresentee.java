package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;

/**
 * Marque de l'opérateur, en France ou à l'export (§4.3.2.3 p.19, C.1.4 p.103, RG-OPE-16).
 *
 * @param marque        identifiant de la marque dans {@code referentiels}
 * @param diffusableWeb faux par défaut
 */
public record MarquePresentee(Portee portee, long marque, boolean diffusableWeb) {

    public MarquePresentee {
        if (portee == null) {
            throw new RegleEnfreinte("RG-OPE-16", "La portée d'une marque est obligatoire : France ou Export.");
        }
    }
}
