package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.GrilleCotisation;
import fr.bnia.declarations.domain.TauxCotisation;
import java.util.ArrayList;
import java.util.List;

/** La grille CVO en mémoire : chaque chargement rend une copie, comme l'adapter. */
final class GrilleEnMemoire implements GrilleCotisationRepository {

    private List<TauxCotisation> taux = new ArrayList<>();

    @Override
    public GrilleCotisation charger() {
        return GrilleCotisation.de(taux);
    }

    @Override
    public void enregistrer(GrilleCotisation grille) {
        taux = new ArrayList<>(grille.taux());
    }
}
