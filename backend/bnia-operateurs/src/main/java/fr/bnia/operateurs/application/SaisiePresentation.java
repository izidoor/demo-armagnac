package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.CarteBancaire;
import fr.bnia.operateurs.domain.Distinction;
import fr.bnia.operateurs.domain.ModePresentation;
import fr.bnia.operateurs.domain.Portee;
import java.util.List;
import java.util.Set;

/**
 * Saisie de la présentation (onglets Marques, Visites et labels, Œnotourisme). Les références vers
 * {@code referentiels} sont des identifiants.
 */
public record SaisiePresentation(
        List<Marque> marques,
        List<Circuit> circuits,
        List<Visite> visites,
        List<Label> labels,
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
        String presentationEn,
        List<Medaille> medailles) {

    public record Marque(Portee portee, long marque, boolean diffusableWeb) {
    }

    public record Circuit(Portee portee, long circuit) {
    }

    public record Visite(long typeVisite, String commentaire) {
    }

    public record Label(long label, String commentaire) {
    }

    public record Medaille(String concours, Integer annee, Distinction distinction, String produit) {
    }
}
