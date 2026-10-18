package fr.bnia.messagerie.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.util.Objects;

/**
 * Sujet d'une conversation libre (spec 15 §3.3) : un titre et l'utilisateur du portail qui l'a ouverte, seul à la
 * voir dans son opérateur (RG-MSG-02, 12).
 */
public record SujetLibre(String titre, Auteur auteur) implements Sujet {

    /** RG-MSG-12 : longueur maximale du titre. */
    public static final int TITRE_MAXIMAL = 120;

    public SujetLibre {
        Objects.requireNonNull(auteur, "auteur");
        if (titre == null || titre.isBlank()) {
            throw new RegleEnfreinte("RG-MSG-12", "Une conversation a un titre.");
        }
        titre = titre.strip();
        if (titre.length() > TITRE_MAXIMAL) {
            throw new RegleEnfreinte("RG-MSG-12", "Le titre compte " + titre.length() + " caractères ; "
                    + TITRE_MAXIMAL + " au plus.");
        }
    }

    @Override
    public String intitule() {
        return titre;
    }
}
