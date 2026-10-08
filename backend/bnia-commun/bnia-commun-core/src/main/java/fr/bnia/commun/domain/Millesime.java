package fr.bnia.commun.domain;

/**
 * Millésime (glossaire, p.95 et 98) : année de la campagne de distillation ; la campagne 2025/2026
 * donne le millésime 2025. Concept partagé par plusieurs contextes (ADR-009, A-OPE-4 de la spec 02).
 *
 * <p>Une année de 1800 à 2100 : le domaine n'a pas d'horloge, et un vieux millésime (XIXe siècle) reste
 * une valeur réelle. Un contexte qui doit refuser un millésime futur le compare lui-même à la date du
 * jour.
 */
public record Millesime(int annee) implements Comparable<Millesime> {

    public static final int MINIMUM = 1800;
    public static final int MAXIMUM = 2100;

    /**
     * @throws RegleEnfreinte si l'année est hors de {@value #MINIMUM} à {@value #MAXIMUM}
     */
    public Millesime {
        if (annee < MINIMUM || annee > MAXIMUM) {
            throw new RegleEnfreinte(null,
                    "Millésime invalide : " + annee + " (attendu : une année de " + MINIMUM + " à " + MAXIMUM + ").");
        }
    }

    public static Millesime de(int annee) {
        return new Millesime(annee);
    }

    @Override
    public int compareTo(Millesime autre) {
        return Integer.compare(annee, autre.annee);
    }

    @Override
    public String toString() {
        return Integer.toString(annee);
    }
}
