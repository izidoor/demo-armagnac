package fr.bnia.referentiels.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.referentiels.domain.ZoneCommerciale;

@UseCase
public class GererZonesCommerciales extends GererValeursSimples<ZoneCommerciale> {

    public GererZonesCommerciales(ZoneCommercialeRepository repository,
            ValeursSimplesLecture<ZoneCommerciale> lecture,
            ControleParametrage controle) {
        super(repository, lecture, ZoneCommerciale::creer, "zones commerciales", controle);
    }
}
