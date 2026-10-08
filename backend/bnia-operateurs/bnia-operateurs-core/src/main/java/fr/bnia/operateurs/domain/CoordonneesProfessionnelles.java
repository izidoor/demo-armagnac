package fr.bnia.operateurs.domain;

import java.util.Set;

/**
 * Contenu d'une fiche professionnelle (C.1.9.1 p.104-105, spec 02 §3.2.3) : tout est facultatif
 * (RG-OPE-27).
 *
 * @param centresInteret identifiants des centres d'intérêt dans {@code referentiels} (ciblage des notifications, C.14)
 * @param pays           identifiant du pays dans {@code referentiels}, ou {@code null}
 */
public record CoordonneesProfessionnelles(
        String fonction,
        String telephoneProfessionnel,
        String telephoneSecondaire,
        String adresse,
        String codePostal,
        String ville,
        Long pays,
        Set<Long> centresInteret,
        String observation) {

    public CoordonneesProfessionnelles {
        fonction = Textes.facultatif(fonction);
        telephoneProfessionnel = Textes.facultatif(telephoneProfessionnel);
        telephoneSecondaire = Textes.facultatif(telephoneSecondaire);
        adresse = Textes.facultatif(adresse);
        codePostal = Textes.facultatif(codePostal);
        ville = Textes.facultatif(ville);
        centresInteret = centresInteret == null ? Set.of() : Set.copyOf(centresInteret);
        observation = Textes.facultatif(observation);
    }

    public static CoordonneesProfessionnelles vides() {
        return new CoordonneesProfessionnelles(null, null, null, null, null, null, null, Set.of(), null);
    }

    /** Fiche d'une personne nouvelle invitée : sa fonction et son téléphone professionnel (RG-OPE-42). */
    public static CoordonneesProfessionnelles de(String fonction, String telephoneProfessionnel) {
        return new CoordonneesProfessionnelles(fonction, telephoneProfessionnel, null, null, null, null, null, Set.of(),
                null);
    }
}
