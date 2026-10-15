package fr.bnia.facturation.api;

import fr.bnia.commun.domain.RegleEnfreinte;

/**
 * Un client de l'hôte (un opérateur), opaque pour {@code facturation} : sans clé étrangère (A-FAC-8), son
 * acheteur se lit par le port {@code Clients} (RG-FAC-11).
 *
 * @param identifiant identifiant chez l'hôte (identifiant de l'opérateur)
 * @param code        code affiché, n° BNIA de l'opérateur
 */
public record ClientRef(String identifiant, String code) {

    public ClientRef {
        if (identifiant == null || identifiant.isBlank() || code == null || code.isBlank()) {
            throw new RegleEnfreinte("RG-FAC-11", "Un client a un identifiant et un code.");
        }
    }

    /** Le client d'un opérateur, désigné par son identifiant et son code. */
    public static ClientRef operateur(long id, String code) {
        return new ClientRef(Long.toString(id), code);
    }
}
