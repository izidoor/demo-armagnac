package fr.bnia.referentiels.domain;

/**
 * Circuit de distribution d'un opérateur (C.10.9.2).
 */
public final class CircuitDistribution extends ValeurSimple {

    private CircuitDistribution(Code code, Libelle libelle, boolean actif) {
        super(code, libelle, actif);
    }

    public static CircuitDistribution creer(Code code, Libelle libelle) {
        return new CircuitDistribution(code, libelle, true);
    }

    public static CircuitDistribution reconstituer(Code code, Libelle libelle, boolean actif) {
        return new CircuitDistribution(code, libelle, actif);
    }
}
