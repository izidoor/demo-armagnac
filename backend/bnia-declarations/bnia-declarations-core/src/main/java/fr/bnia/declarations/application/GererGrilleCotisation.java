package fr.bnia.declarations.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.declarations.domain.GrilleCotisation;
import fr.bnia.declarations.domain.GrilleCotisation.TauxEnVigueur;
import fr.bnia.declarations.domain.TauxCotisation;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.Module;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

/**
 * La grille des taux CVO, dans le menu Paramétrage du back-office (spec 05 §3.5, §6.1, décision du 2026-10-13) :
 * lue par le personnel du BNIA, gérée par ses agents et administrateurs (module Paramétrage). RG-DEC-30.
 * Chaque écriture rend la chaîne recomposée.
 */
@UseCase
public class GererGrilleCotisation {

    /** Saisie d'un taux CVO. */
    public record SaisieTauxCvo(BigDecimal montant, BigDecimal pourcentageExonere, LocalDate debut) {

        TauxCotisation taux() {
            return new TauxCotisation(montant, pourcentageExonere, debut);
        }
    }

    private final GrilleCotisationRepository grilles;
    private final OperateursLecture operateurs;

    public GererGrilleCotisation(GrilleCotisationRepository grilles, OperateursLecture operateurs) {
        this.grilles = grilles;
        this.operateurs = operateurs;
    }

    /** La chaîne des taux, du plus ancien au plus récent, avec leur fin calculée. */
    public List<TauxEnVigueur> consulter(Acteur acteur) {
        if (!droitsBnia(acteur).lit(Module.PARAMETRAGE)) {
            throw new AccesRefuse("RG-OPE-31", "La grille CVO est réservée au personnel du BNIA.");
        }
        return grilles.charger().chaine();
    }

    /** RG-DEC-30. */
    public List<TauxEnVigueur> ajouter(Acteur acteur, SaisieTauxCvo saisie) {
        exigerGestion(acteur);
        TauxCotisation taux = saisie.taux();
        return modifierGrille(grille -> grille.ajouter(taux));
    }

    /** RG-DEC-30 : le taux est désigné par sa date de début actuelle. */
    public List<TauxEnVigueur> modifier(Acteur acteur, LocalDate debutActuel, SaisieTauxCvo saisie) {
        exigerGestion(acteur);
        TauxCotisation taux = saisie.taux();
        return modifierGrille(grille -> grille.modifier(debutActuel, taux));
    }

    /** RG-DEC-30 : seules les DSM non facturées en dépendent (H-DEC-20). */
    public List<TauxEnVigueur> supprimer(Acteur acteur, LocalDate debut) {
        exigerGestion(acteur);
        return modifierGrille(grille -> grille.supprimer(debut));
    }

    private List<TauxEnVigueur> modifierGrille(Consumer<GrilleCotisation> modification) {
        GrilleCotisation grille = grilles.charger();
        modification.accept(grille);
        grilles.enregistrer(grille);
        return grille.chaine();
    }

    private void exigerGestion(Acteur acteur) {
        if (!droitsBnia(acteur).gere(Module.PARAMETRAGE)) {
            throw new AccesRefuse("RG-OPE-31", "Le Paramétrage est réservé aux agents et aux administrateurs du BNIA.");
        }
    }

    private Droits droitsBnia(Acteur acteur) {
        return operateurs.droits(acteur.utilisateur(), operateurs.operateurBnia());
    }
}
