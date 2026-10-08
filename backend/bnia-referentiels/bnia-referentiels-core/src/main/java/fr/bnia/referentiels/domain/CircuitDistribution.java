package fr.bnia.referentiels.domain;

/**
 * Circuit de distribution d'un opérateur (C.10.9.2).
 */
public final class CircuitDistribution extends ValeurSimple {

    private CircuitDistribution(Long id, Code code, Libelle libelle, boolean actif) {
        super(id, code, libelle, actif);
    }

    public static CircuitDistribution creer(Code code, Libelle libelle) {
        return new CircuitDistribution(null, code, libelle, true);
    }

    public static CircuitDistribution reconstituer(Long id, Code code, Libelle libelle, boolean actif) {
        return new CircuitDistribution(id, code, libelle, actif);
    }
}
