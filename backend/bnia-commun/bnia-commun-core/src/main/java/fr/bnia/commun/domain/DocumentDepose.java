package fr.bnia.commun.domain;

import java.util.Objects;

/**
 * Référence d'un fichier déposé dans le stockage commun (ADR-018) : les contextes ne gardent que cette
 * référence, jamais le contenu.
 *
 * @param cle    identifiant du document dans le stockage (clé étrangère vers {@code documents.document})
 * @param nom    nom du fichier tel que déposé
 * @param type   type de contenu ({@code application/pdf})
 * @param taille taille en octets
 */
public record DocumentDepose(long cle, String nom, String type, long taille) {

    public DocumentDepose {
        Objects.requireNonNull(nom, "nom");
        Objects.requireNonNull(type, "type");
        if (taille < 0) {
            throw new IllegalArgumentException("Taille négative : " + taille);
        }
    }
}
