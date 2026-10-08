package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;

/**
 * Titre, nom et prénom d'une personne : d'un contact, propriété de l'opérateur, ou d'un utilisateur,
 * propriété de la personne (vision §2.1). Le nom est toujours obligatoire ; le prénom l'est pour un
 * utilisateur (RG-OPE-27).
 *
 * @param titre identifiant du titre dans {@code referentiels}, ou {@code null}
 */
public record IdentitePersonne(Long titre, String nom, String prenom) {

    public IdentitePersonne {
        nom = Textes.facultatif(nom);
        prenom = Textes.facultatif(prenom);
        if (nom == null) {
            throw new RegleEnfreinte("RG-OPE-27", "Le nom est obligatoire.");
        }
    }

    /** RG-OPE-27 : nom et prénom obligatoires, titre facultatif. */
    public static IdentitePersonne utilisateur(Long titre, String nom, String prenom) {
        IdentitePersonne identite = new IdentitePersonne(titre, nom, prenom);
        if (identite.prenom() == null) {
            throw new RegleEnfreinte("RG-OPE-27", "Le prénom est obligatoire.");
        }
        return identite;
    }

    /** « Jeanne Durand », ou le nom seul. */
    public String nomComplet() {
        return prenom == null ? nom : prenom + " " + nom;
    }
}
