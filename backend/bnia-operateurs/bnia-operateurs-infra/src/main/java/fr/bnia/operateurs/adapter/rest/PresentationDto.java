package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.SaisiePresentation;
import fr.bnia.operateurs.domain.CarteBancaire;
import fr.bnia.operateurs.domain.Distinction;
import fr.bnia.operateurs.domain.ModePresentation;
import fr.bnia.operateurs.domain.Oenotourisme;
import fr.bnia.operateurs.domain.Portee;
import fr.bnia.operateurs.domain.PresentationOperateur;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Set;

/**
 * Présentation de l'opérateur, même forme en lecture et en saisie : l'écran renvoie ce qu'il a reçu,
 * modifié (onglets Marques, Visites et labels, Œnotourisme).
 */
@Schema(name = "PresentationOperateur", description = "Marques, circuits, visites, labels, œnotourisme, médailles (C.1.4 à C.1.7)")
public record PresentationDto(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Une marque au plus une fois par portée (RG-OPE-16)")
        List<Marque> marques,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Un circuit au plus une fois par portée (RG-OPE-16)")
        List<Circuit> circuits,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Un type de visite au plus une fois (RG-OPE-17)")
        List<Visite> visites,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Un label au plus une fois (RG-OPE-17)")
        List<Label> labels,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean paiementCarte,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Vidées sans paiement par carte (RG-OPE-18)")
        Set<CarteBancaire> cartes,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean degustationGratuite,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean lieuDeVente,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean accueilGroupes,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean adhesionOenotourisme,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Identifiants des langues parlées") Set<Long> langues,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Identifiants des produits commercialisés")
        Set<Long> produitsCommercialises,
        ModePresentation modePresentation,
        String presentationFr,
        String presentationEn,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<Medaille> medailles) {

    @Schema(name = "MarquePresentee")
    public record Marque(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Portee portee,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Identifiant de la marque") long marque,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean diffusableWeb) {
    }

    @Schema(name = "CircuitPresente")
    public record Circuit(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Portee portee,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Identifiant du circuit de distribution") long circuit) {
    }

    @Schema(name = "VisiteProposee")
    public record Visite(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Identifiant du type de visite") long typeVisite,
            String commentaire) {
    }

    @Schema(name = "LabelObtenu")
    public record Label(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Identifiant du label") long label,
            String commentaire) {
    }

    @Schema(name = "MedailleConcours")
    public record Medaille(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Concours général agricole") String concours,
            @Schema(description = "Un millésime valide (RG-OPE-19)", example = "2024") Integer annee,
            Distinction distinction,
            String produit) {
    }

    static PresentationDto depuis(PresentationOperateur p) {
        Oenotourisme o = p.oenotourisme();
        return new PresentationDto(
                p.marques().stream().map(m -> new Marque(m.portee(), m.marque(), m.diffusableWeb())).toList(),
                p.circuits().stream().map(c -> new Circuit(c.portee(), c.circuit())).toList(),
                p.visites().stream().map(v -> new Visite(v.typeVisite(), v.commentaire())).toList(),
                p.labels().stream().map(l -> new Label(l.label(), l.commentaire())).toList(),
                o.paiementCarte(), o.cartes(), o.degustationGratuite(), o.lieuDeVente(), o.accueilGroupes(),
                o.adhesionOenotourisme(), o.langues(), o.produitsCommercialises(), o.modePresentation(),
                o.presentationFr(), o.presentationEn(),
                p.medailles().stream().map(m -> new Medaille(m.concours(), m.annee() == null ? null : m.annee().annee(),
                        m.distinction(), m.produit())).toList());
    }

    SaisiePresentation versSaisie() {
        return new SaisiePresentation(
                liste(marques).stream().map(m -> new SaisiePresentation.Marque(m.portee(), m.marque(), m.diffusableWeb())).toList(),
                liste(circuits).stream().map(c -> new SaisiePresentation.Circuit(c.portee(), c.circuit())).toList(),
                liste(visites).stream().map(v -> new SaisiePresentation.Visite(v.typeVisite(), v.commentaire())).toList(),
                liste(labels).stream().map(l -> new SaisiePresentation.Label(l.label(), l.commentaire())).toList(),
                paiementCarte, cartes, degustationGratuite, lieuDeVente, accueilGroupes, adhesionOenotourisme, langues,
                produitsCommercialises, modePresentation, presentationFr, presentationEn,
                liste(medailles).stream().map(m -> new SaisiePresentation.Medaille(m.concours(), m.annee(), m.distinction(),
                        m.produit())).toList());
    }

    private static <T> List<T> liste(List<T> elements) {
        return elements == null ? List.of() : elements;
    }
}
