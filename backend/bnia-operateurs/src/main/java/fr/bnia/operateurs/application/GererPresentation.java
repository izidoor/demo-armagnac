package fr.bnia.operateurs.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.CircuitPresente;
import fr.bnia.operateurs.domain.LabelObtenu;
import fr.bnia.operateurs.domain.MarquePresentee;
import fr.bnia.operateurs.domain.MedailleConcours;
import fr.bnia.operateurs.domain.Oenotourisme;
import fr.bnia.operateurs.domain.PresentationOperateur;
import fr.bnia.operateurs.domain.VisiteProposee;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import fr.bnia.referentiels.domain.partage.ValeurVue;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.LongFunction;
import java.util.stream.Collectors;

/**
 * Présentation de l'opérateur (§4.3.2.3 à 4.3.2.5 p.19-20, C.1.4 à C.1.7 p.103-104), au back-office et
 * au portail. Une marque absente du référentiel se crée depuis la fiche par l'écran de
 * {@code referentiels} (RG-REF-14).
 */
@UseCase
public class GererPresentation {

    private final PresentationRepository repository;
    private final OperateurRepository operateurs;
    private final ReferentielsLecture referentiels;

    public GererPresentation(PresentationRepository repository, OperateurRepository operateurs,
            ReferentielsLecture referentiels) {
        this.repository = repository;
        this.operateurs = operateurs;
        this.referentiels = referentiels;
    }

    /** Vide tant que rien n'a été saisi. */
    public PresentationOperateur consulter(long operateur) {
        verifierOperateur(operateur);
        return repository.parId(operateur).orElseGet(() -> PresentationOperateur.vide(operateur));
    }

    /** RG-OPE-16 à 19, 21. */
    public PresentationOperateur modifier(long operateur, SaisiePresentation saisie) {
        PresentationOperateur presentation = consulter(operateur);
        verifierReferences(saisie, presentation);
        presentation.modifier(
                liste(saisie.marques()).stream().map(m -> new MarquePresentee(m.portee(), m.marque(), m.diffusableWeb())).toList(),
                liste(saisie.circuits()).stream().map(c -> new CircuitPresente(c.portee(), c.circuit())).toList(),
                liste(saisie.visites()).stream().map(v -> new VisiteProposee(v.typeVisite(), v.commentaire())).toList(),
                liste(saisie.labels()).stream().map(l -> new LabelObtenu(l.label(), l.commentaire())).toList(),
                new Oenotourisme(saisie.paiementCarte(), saisie.cartes(), saisie.degustationGratuite(),
                        saisie.lieuDeVente(), saisie.accueilGroupes(), saisie.adhesionOenotourisme(), saisie.langues(),
                        saisie.produitsCommercialises(), saisie.modePresentation(), saisie.presentationFr(),
                        saisie.presentationEn()),
                liste(saisie.medailles()).stream()
                        .map(m -> MedailleConcours.saisie(m.concours(), m.annee(), m.distinction(), m.produit()))
                        .toList());
        repository.enregistrer(presentation);
        return presentation;
    }

    /** RG-OPE-21 : une valeur nouvellement choisie doit être active ; celles déjà portées restent acceptées. */
    private void verifierReferences(SaisiePresentation saisie, PresentationOperateur actuelle) {
        verifier("marque", ids(liste(saisie.marques()).stream().map(SaisiePresentation.Marque::marque).toList()),
                ids(actuelle.marques().stream().map(MarquePresentee::marque).toList()), referentiels::marque);
        verifier("circuit de distribution", ids(liste(saisie.circuits()).stream().map(SaisiePresentation.Circuit::circuit).toList()),
                ids(actuelle.circuits().stream().map(CircuitPresente::circuit).toList()), referentiels::circuitDistribution);
        verifier("type de visite", ids(liste(saisie.visites()).stream().map(SaisiePresentation.Visite::typeVisite).toList()),
                ids(actuelle.visites().stream().map(VisiteProposee::typeVisite).toList()), referentiels::typeVisite);
        verifier("label", ids(liste(saisie.labels()).stream().map(SaisiePresentation.Label::label).toList()),
                ids(actuelle.labels().stream().map(LabelObtenu::label).toList()), referentiels::label);
        verifier("langue", ids(saisie.langues()), actuelle.oenotourisme().langues(), referentiels::langue);
        verifier("produit commercialisé", ids(saisie.produitsCommercialises()),
                actuelle.oenotourisme().produitsCommercialises(), referentiels::produitCommercialise);
    }

    private static void verifier(String nom, Set<Long> choisis, Set<Long> dejaPortes,
            LongFunction<Optional<ValeurVue>> lire) {
        for (long id : choisis) {
            if (dejaPortes.contains(id)) {
                continue;
            }
            ValeurVue valeur = lire.apply(id).orElseThrow(() -> new RegleEnfreinte("RG-OPE-21",
                    "Valeur inconnue (" + nom + ") : " + id + "."));
            if (!valeur.actif()) {
                throw new RegleEnfreinte("RG-OPE-21",
                        "Valeur inactive (" + nom + ") : « " + valeur.libelle() + " ». Choisissez une valeur active.");
            }
        }
    }

    private void verifierOperateur(long operateur) {
        if (operateurs.parId(operateur).isEmpty()) {
            throw new ElementIntrouvable("Opérateur " + operateur + " introuvable.");
        }
    }

    private static Set<Long> ids(Collection<Long> ids) {
        return ids == null ? Set.of() : ids.stream().collect(Collectors.toSet());
    }

    private static <T> List<T> liste(List<T> elements) {
        return elements == null ? List.of() : elements;
    }
}
