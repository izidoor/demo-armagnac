package fr.bnia.declarations.application;

import java.util.Objects;

/** Un fichier reçu pour dépôt, tel que l'adapter REST le transmet : nom et contenu. */
public record FichierDepose(String nom, byte[] contenu) {

    public FichierDepose {
        Objects.requireNonNull(contenu, "contenu");
        nom = nom == null || nom.isBlank() ? "document.pdf" : nom.strip();
    }
}
