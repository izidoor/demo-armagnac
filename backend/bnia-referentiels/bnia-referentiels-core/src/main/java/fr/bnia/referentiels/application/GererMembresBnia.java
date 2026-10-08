package fr.bnia.referentiels.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.referentiels.domain.MembreBnia;

@UseCase
public class GererMembresBnia extends GererValeursSimples<MembreBnia> {

    public GererMembresBnia(MembreBniaRepository repository, ValeursSimplesLecture<MembreBnia> lecture) {
        super(repository, lecture, MembreBnia::creer, "membres BNIA");
    }
}
