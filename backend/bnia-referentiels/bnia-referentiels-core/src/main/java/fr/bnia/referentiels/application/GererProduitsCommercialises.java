package fr.bnia.referentiels.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.referentiels.domain.ProduitCommercialise;

@UseCase
public class GererProduitsCommercialises extends GererValeursSimples<ProduitCommercialise> {

    public GererProduitsCommercialises(ProduitCommercialiseRepository repository, ValeursSimplesLecture<ProduitCommercialise> lecture,
            ControleParametrage controle) {
        super(repository, lecture, ProduitCommercialise::creer, "produits commercialisés", controle);
    }
}
