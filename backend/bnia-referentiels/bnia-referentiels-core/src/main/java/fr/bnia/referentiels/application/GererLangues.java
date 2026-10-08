package fr.bnia.referentiels.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.referentiels.domain.Langue;

@UseCase
public class GererLangues extends GererValeursSimples<Langue> {

    public GererLangues(LangueRepository repository, ValeursSimplesLecture<Langue> lecture,
            ControleParametrage controle) {
        super(repository, lecture, Langue::creer, "langues", controle);
    }
}
