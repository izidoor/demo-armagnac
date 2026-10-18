package fr.bnia.messagerie.domain;

import fr.bnia.messagerie.api.ObjetRef;
import fr.bnia.messagerie.api.TypeObjet;
import java.util.Objects;

/**
 * Sujet d'une conversation d'objet (spec 15 §3.2) : l'objet désigné chez son contexte, sans clé étrangère, et son
 * libellé copié à l'ouverture (RG-MSG-04).
 */
public record ObjetMetier(ObjetRef objet, String libelle) implements Sujet {

    public ObjetMetier {
        Objects.requireNonNull(objet, "objet");
        Objects.requireNonNull(libelle, "libelle");
    }

    public TypeObjet type() {
        return objet.type();
    }

    @Override
    public String intitule() {
        return libelle;
    }
}
