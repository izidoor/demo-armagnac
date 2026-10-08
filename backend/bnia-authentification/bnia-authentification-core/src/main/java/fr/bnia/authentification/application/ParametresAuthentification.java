package fr.bnia.authentification.application;

import fr.bnia.authentification.domain.Politique;

/**
 * Configuration du contexte (RG-AUT-14).
 *
 * @param urlPublique adresse du front, pour les liens des e-mails
 */
public record ParametresAuthentification(Politique politique, String urlPublique) {

    public ParametresAuthentification {
        urlPublique = urlPublique.endsWith("/") ? urlPublique.substring(0, urlPublique.length() - 1) : urlPublique;
    }
}
