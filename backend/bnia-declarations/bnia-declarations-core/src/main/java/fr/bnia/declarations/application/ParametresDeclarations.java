package fr.bnia.declarations.application;

/**
 * Valeurs de configuration du contexte, non modifiables à l'écran.
 *
 * @param classeTvaCvo code de la classe de TVA de la cotisation CVO, lue dans {@code referentiels} pour la TVA
 *                     indicative (RG-DEC-34, H-DEC-22) : {@code NORMAL}
 */
public record ParametresDeclarations(String classeTvaCvo) {
}
