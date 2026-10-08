package fr.bnia.operateurs.application;

import java.time.Duration;

/**
 * Valeurs de configuration du contexte, non modifiables à l'écran (décision du 2026-10-08).
 *
 * @param urlPublique              adresse du front, pour les liens des e-mails ({@code https://bnia.example})
 * @param validiteInvitation       RG-OPE-40 : 7 jours
 * @param validiteChangementEmail  RG-OPE-48 : 24 heures
 * @param delaiPremierMotDePasse   RG-AUT-15 : une heure après l'acceptation d'une invitation
 */
public record ParametresOperateurs(String urlPublique, Duration validiteInvitation, Duration validiteChangementEmail,
        Duration delaiPremierMotDePasse) {

    public ParametresOperateurs {
        urlPublique = urlPublique.endsWith("/") ? urlPublique.substring(0, urlPublique.length() - 1) : urlPublique;
    }
}
