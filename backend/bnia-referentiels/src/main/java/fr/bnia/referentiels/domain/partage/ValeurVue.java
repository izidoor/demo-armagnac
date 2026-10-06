package fr.bnia.referentiels.domain.partage;

/**
 * Valeur d'une liste de référentiel, vue par les autres contextes : titre, label, marque, zone
 * commerciale, classe de TVA… Une valeur inactive reste lisible là où elle est déjà utilisée, mais
 * n'est plus proposée à la saisie (RG-REF-04).
 */
public record ValeurVue(String code, String libelle, boolean actif) {
}
