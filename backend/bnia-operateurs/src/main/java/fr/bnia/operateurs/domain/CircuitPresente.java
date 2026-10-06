package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;

/**
 * Circuit de distribution de l'opérateur, en France ou à l'export (C.1.4 p.103, RG-OPE-16).
 *
 * @param circuit identifiant du circuit dans {@code referentiels}
 */
public record CircuitPresente(Portee portee, long circuit) {

    public CircuitPresente {
        if (portee == null) {
            throw new RegleEnfreinte("RG-OPE-16", "La portée d'un circuit est obligatoire : France ou Export.");
        }
    }
}
