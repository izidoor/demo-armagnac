package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.util.Set;

/**
 * Identité et coordonnées d'un contact (C.1.9.1 p.104-105) : seul le nom est obligatoire (RG-OPE-27).
 *
 * @param titre          identifiant du titre dans {@code referentiels}, ou {@code null}
 * @param centresInteret identifiants des centres d'intérêt dans {@code referentiels}
 * @param email          format contrôlé (RG-OPE-28), requis pour l'accès au portail (RG-OPE-29)
 * @param pays           identifiant du pays dans {@code referentiels}, ou {@code null}
 */
public record IdentiteContact(
        Long titre,
        String nom,
        String prenom,
        String fonction,
        Set<Long> centresInteret,
        String telephoneProfessionnel,
        String telephoneSecondaire,
        Email email,
        String adresse,
        String codePostal,
        String ville,
        Long pays,
        String observation) {

    public IdentiteContact {
        nom = Textes.facultatif(nom);
        if (nom == null) {
            throw new RegleEnfreinte("RG-OPE-27", "Le nom du contact est obligatoire.");
        }
        prenom = Textes.facultatif(prenom);
        fonction = Textes.facultatif(fonction);
        centresInteret = centresInteret == null ? Set.of() : Set.copyOf(centresInteret);
        telephoneProfessionnel = Textes.facultatif(telephoneProfessionnel);
        telephoneSecondaire = Textes.facultatif(telephoneSecondaire);
        adresse = Textes.facultatif(adresse);
        codePostal = Textes.facultatif(codePostal);
        ville = Textes.facultatif(ville);
        observation = Textes.facultatif(observation);
    }

    /** « Jeanne Durand », ou le nom seul. */
    public String nomComplet() {
        return prenom == null ? nom : prenom + " " + nom;
    }

    IdentiteContact avecEmail(Email nouveau) {
        return new IdentiteContact(titre, nom, prenom, fonction, centresInteret, telephoneProfessionnel,
                telephoneSecondaire, nouveau, adresse, codePostal, ville, pays, observation);
    }
}
