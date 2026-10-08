package fr.bnia.referentiels.domain.partage;

/**
 * Valeur d'une liste de référentiel, vue par les autres contextes : titre, label, marque, zone
 * commerciale, classe de TVA… Une valeur inactive reste lisible là où elle est déjà utilisée, mais
 * n'est plus proposée à la saisie (RG-REF-04).
 *
 * @param id identifiant technique, celui que les autres contextes stockent (ADR-011)
 */
public record ValeurVue(long id, String code, String libelle, boolean actif) {
}
