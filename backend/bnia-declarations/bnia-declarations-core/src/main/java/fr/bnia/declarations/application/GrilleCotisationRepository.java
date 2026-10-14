package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.GrilleCotisation;

/** Charge et enregistre la grille CVO d'un bloc (agrégat, spec 05 §3.5). */
public interface GrilleCotisationRepository {

    /** La grille, vide tant qu'aucun taux n'est saisi. */
    GrilleCotisation charger();

    void enregistrer(GrilleCotisation grille);
}
