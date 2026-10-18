package fr.bnia.messagerie.api;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.util.Objects;

/**
 * Un objet métier désigné chez son contexte, opaque pour {@code messagerie} : sans clé étrangère (A-MSG-5).
 *
 * @param type        type de l'objet
 * @param identifiant identifiant de l'objet chez son contexte
 */
public record ObjetRef(TypeObjet type, String identifiant) {

    public ObjetRef {
        Objects.requireNonNull(type, "type");
        if (identifiant == null || identifiant.isBlank()) {
            throw new RegleEnfreinte("RG-MSG-04", "Un objet métier a un identifiant.");
        }
    }

    /** Un objet désigné par son identifiant technique. */
    public static ObjetRef de(TypeObjet type, long id) {
        return new ObjetRef(type, Long.toString(id));
    }
}
