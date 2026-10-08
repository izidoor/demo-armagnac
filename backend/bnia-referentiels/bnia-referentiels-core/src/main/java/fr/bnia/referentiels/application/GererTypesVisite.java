package fr.bnia.referentiels.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.referentiels.domain.TypeVisite;

@UseCase
public class GererTypesVisite extends GererValeursSimples<TypeVisite> {

    public GererTypesVisite(TypeVisiteRepository repository, ValeursSimplesLecture<TypeVisite> lecture,
            ControleParametrage controle) {
        super(repository, lecture, TypeVisite::creer, "types de visite", controle);
    }
}
