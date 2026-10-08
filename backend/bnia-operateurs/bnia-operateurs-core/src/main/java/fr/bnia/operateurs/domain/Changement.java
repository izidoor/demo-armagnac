package fr.bnia.operateurs.domain;

/**
 * Un champ changé, avec ses valeurs avant et après (RG-OPE-34). Les références vers {@code referentiels}
 * sont notées par leurs identifiants, séparés par des virgules ; la couche application les remplace par
 * leurs libellés.
 *
 * @param avant {@code null} si le champ était vide
 * @param apres {@code null} si le champ est vidé
 */
public record Changement(ChampContact champ, String avant, String apres) {
}
