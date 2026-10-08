package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.Millesime;
import fr.bnia.commun.domain.RegleEnfreinte;

/**
 * Médaille obtenue à un concours, semi-structurée (§4.3.2.5 p.20, C.1.7.1 p.104, décision du 2026-10-06) :
 * concours obligatoire ; année, distinction et produit facultatifs (RG-OPE-19).
 *
 * @param annee       un millésime valide, ou {@code null}
 * @param distinction {@code null} si non précisée
 * @param produit     produit primé, texte libre
 */
public record MedailleConcours(String concours, Millesime annee, Distinction distinction, String produit) {

    public MedailleConcours {
        concours = Textes.facultatif(concours);
        if (concours == null) {
            throw new RegleEnfreinte("RG-OPE-19", "Le concours d'une médaille est obligatoire.");
        }
        produit = Textes.facultatif(produit);
    }

    /** RG-OPE-19 : l'année, si elle est saisie, est un millésime valide. */
    public static MedailleConcours saisie(String concours, Integer annee, Distinction distinction, String produit) {
        Millesime millesime = null;
        if (annee != null) {
            try {
                millesime = Millesime.de(annee);
            } catch (RegleEnfreinte invalide) {
                throw new RegleEnfreinte("RG-OPE-19", invalide.getMessage());
            }
        }
        return new MedailleConcours(concours, millesime, distinction, produit);
    }
}
