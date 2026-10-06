package fr.bnia.operateurs.domain;

/**
 * Adresse postale de l'opérateur (C.1.1.1 p.101). Tout est facultatif à l'enregistrement ; ce qui
 * manque est signalé (RG-OPE-02).
 *
 * @param pays identifiant du pays dans {@code referentiels} (ADR-011)
 */
public record Adresse(String ligne1, String ligne2, String ligne3, String codePostal, String ville, Long pays) {

    public Adresse {
        ligne1 = Textes.facultatif(ligne1);
        ligne2 = Textes.facultatif(ligne2);
        ligne3 = Textes.facultatif(ligne3);
        codePostal = Textes.facultatif(codePostal);
        ville = Textes.facultatif(ville);
    }
}
