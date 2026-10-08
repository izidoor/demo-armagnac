package fr.bnia.operateurs.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Œnotourisme (§4.3.2.5 p.20, C.1.7 p.104), RG-OPE-18 : les cartes acceptées n'existent que si le
 * paiement par carte est accepté ; le décocher les vide. Les booléens sont faux par défaut.
 *
 * @param langues              identifiants des langues parlées dans {@code referentiels} (RG-OPE-21)
 * @param produitsCommercialises identifiants des produits commercialisés dans {@code referentiels}
 * @param modePresentation     facultatif
 */
public record Oenotourisme(
        boolean paiementCarte,
        Set<CarteBancaire> cartes,
        boolean degustationGratuite,
        boolean lieuDeVente,
        boolean accueilGroupes,
        boolean adhesionOenotourisme,
        Set<Long> langues,
        Set<Long> produitsCommercialises,
        ModePresentation modePresentation,
        String presentationFr,
        String presentationEn) {

    public Oenotourisme {
        cartes = !paiementCarte || cartes == null || cartes.isEmpty() ? Set.of() : Set.copyOf(EnumSet.copyOf(cartes));
        langues = langues == null ? Set.of() : Set.copyOf(langues);
        produitsCommercialises = produitsCommercialises == null ? Set.of() : Set.copyOf(produitsCommercialises);
        presentationFr = Textes.facultatif(presentationFr);
        presentationEn = Textes.facultatif(presentationEn);
    }

    public static Oenotourisme aucun() {
        return new Oenotourisme(false, Set.of(), false, false, false, false, Set.of(), Set.of(), null, null, null);
    }
}
