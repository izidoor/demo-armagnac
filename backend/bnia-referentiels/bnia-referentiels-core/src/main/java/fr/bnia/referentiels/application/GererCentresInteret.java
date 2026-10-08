package fr.bnia.referentiels.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.referentiels.domain.CentreInteret;

@UseCase
public class GererCentresInteret extends GererValeursSimples<CentreInteret> {

    public GererCentresInteret(CentreInteretRepository repository, ValeursSimplesLecture<CentreInteret> lecture,
            ControleParametrage controle) {
        super(repository, lecture, CentreInteret::creer, "centres d'intérêt", controle);
    }
}
