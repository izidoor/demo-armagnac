package fr.bnia.referentiels.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.referentiels.domain.Titre;

@UseCase
public class GererTitres extends GererValeursSimples<Titre> {

    public GererTitres(TitreRepository repository, ValeursSimplesLecture<Titre> lecture,
            ControleParametrage controle) {
        super(repository, lecture, Titre::creer, "titres", controle);
    }
}
