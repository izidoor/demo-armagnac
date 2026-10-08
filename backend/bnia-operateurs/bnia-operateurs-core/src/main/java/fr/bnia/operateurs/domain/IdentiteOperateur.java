package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;

/**
 * Identité et coordonnées d'un opérateur (C.1.1.1 p.100-101), hors type et code. Seule la raison sociale
 * est obligatoire (RG-OPE-02) ; les textes facultatifs vides valent « non renseigné ».
 *
 * @param titre      identifiant du titre dans {@code referentiels}, ou {@code null}
 * @param membreBnia identifiant du membre BNIA dans {@code referentiels}, ou {@code null} (Q-OPE-4)
 */
public record IdentiteOperateur(
        String groupe,
        Cvi cvi,
        Siret siret,
        NumeroAccise accise,
        String bureauDouanes,
        String identifiantOdg,
        Long titre,
        String raisonSociale,
        String nomCommercial,
        Adresse adresse,
        String telephone1,
        String telephone2,
        String portable,
        Email emailGeneral,
        SiteWeb siteWeb,
        SiteWeb siteCommercial,
        CoordonneesGps gps,
        Long membreBnia,
        boolean accordDiffusionGeneral,
        String commentaireInterne) {

    public IdentiteOperateur {
        raisonSociale = Textes.facultatif(raisonSociale);
        if (raisonSociale == null) {
            throw new RegleEnfreinte("RG-OPE-02", "La raison sociale est obligatoire.");
        }
        groupe = Textes.facultatif(groupe);
        bureauDouanes = Textes.facultatif(bureauDouanes);
        identifiantOdg = Textes.facultatif(identifiantOdg);
        nomCommercial = Textes.facultatif(nomCommercial);
        adresse = adresse == null ? new Adresse(null, null, null, null, null, null) : adresse;
        telephone1 = Textes.facultatif(telephone1);
        telephone2 = Textes.facultatif(telephone2);
        portable = Textes.facultatif(portable);
        commentaireInterne = Textes.facultatif(commentaireInterne);
    }

    /** RG-OPE-36 : la même identité, avec les seuls champs qu'un opérateur modifie lui-même. */
    public IdentiteOperateur avecProfil(ProfilOperateur p) {
        return new IdentiteOperateur(groupe, cvi, siret, accise, bureauDouanes, identifiantOdg, p.titre(), raisonSociale,
                p.nomCommercial(), p.adresse(), p.telephone1(), p.telephone2(), p.portable(), p.emailGeneral(),
                p.siteWeb(), p.siteCommercial(), p.gps(), membreBnia, p.accordDiffusionGeneral(), commentaireInterne);
    }

    /** RG-OPE-08 : forme de comparaison de la raison sociale. */
    public String raisonSocialeNormalisee() {
        return Textes.normalise(raisonSociale);
    }

    /** RG-OPE-09 : deux fiches sont du même groupe si cette forme est égale ; {@code null} sans groupe. */
    public String groupeNormalise() {
        return groupe == null ? null : Textes.normalise(groupe);
    }
}
