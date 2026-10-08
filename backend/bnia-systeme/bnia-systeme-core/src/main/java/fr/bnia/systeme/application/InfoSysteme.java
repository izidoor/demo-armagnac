package fr.bnia.systeme.application;

/**
 * Modèle de lecture de la sonde technique : identifie l'application et la base qui la sert.
 */
public record InfoSysteme(String application, String version, String versionBase) {
}
