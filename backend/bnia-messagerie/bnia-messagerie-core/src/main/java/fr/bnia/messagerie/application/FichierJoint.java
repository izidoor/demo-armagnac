package fr.bnia.messagerie.application;

import java.util.Objects;

/** Un fichier joint à un message, tel que reçu, avant son dépôt dans le stockage commun (RG-MSG-14). */
public record FichierJoint(String nom, byte[] contenu) {

    public FichierJoint {
        Objects.requireNonNull(nom, "nom");
        Objects.requireNonNull(contenu, "contenu");
    }
}
