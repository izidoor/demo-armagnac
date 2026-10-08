package fr.bnia.referentiels.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.referentiels.domain.Label;

@UseCase
public class GererLabels extends GererValeursSimples<Label> {

    public GererLabels(LabelRepository repository, ValeursSimplesLecture<Label> lecture,
            ControleParametrage controle) {
        super(repository, lecture, Label::creer, "labels", controle);
    }
}
