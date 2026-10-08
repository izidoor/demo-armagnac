package fr.bnia.referentiels.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.referentiels.domain.CircuitDistribution;

@UseCase
public class GererCircuitsDistribution extends GererValeursSimples<CircuitDistribution> {

    public GererCircuitsDistribution(CircuitDistributionRepository repository, ValeursSimplesLecture<CircuitDistribution> lecture,
            ControleParametrage controle) {
        super(repository, lecture, CircuitDistribution::creer, "circuits de distribution", controle);
    }
}
