package fr.bnia.operateurs.application;

/**
 * Valeur saisie déjà portée par une autre fiche : avertissement non bloquant (RG-OPE-06, RG-OPE-08).
 *
 * @param regle    {@code RG-OPE-06} (CVI, SIRET, accise) ou {@code RG-OPE-08} (raison sociale)
 * @param operateur la fiche qui porte déjà la valeur
 */
public record Doublon(Champ champ, String regle, OperateurResume operateur) {

    public enum Champ {
        RAISON_SOCIALE, CVI, SIRET, ACCISE
    }
}
