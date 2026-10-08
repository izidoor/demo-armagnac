package fr.bnia.authentification.domain;

import java.time.Duration;
import java.util.Optional;

/**
 * Durées et seuils de l'authentification (RG-AUT-14) : valeurs de configuration, non modifiables à
 * l'écran (décision du 2026-10-08).
 *
 * @param validiteCode          RG-AUT-05 : 10 minutes
 * @param validiteLien          RG-AUT-08 : 1 heure
 * @param validiteConfiance     RG-AUT-06 : 30 jours
 * @param echecsAvantBlocage    RG-AUT-03 : 3
 * @param dureeBlocage          RG-AUT-03 : 30 minutes
 * @param longueurMotDePasse    RG-AUT-07 : 12 caractères
 * @param codeFixe              RG-AUT-01 : {@code 000000} en profils {@code dev} et {@code demo} ; vide ailleurs
 */
public record Politique(Duration validiteCode, Duration validiteLien, Duration validiteConfiance,
        int echecsAvantBlocage, Duration dureeBlocage, int longueurMotDePasse, Optional<String> codeFixe) {
}
