package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import java.util.Map;

/**
 * Une ligne de DSM telle que saisie à l'écran, contrôlée par la DSM (RG-DEC-10 à 13, 15, 16).
 *
 * @param id      ligne existante, ou {@code null} pour une ligne ajoutée
 * @param compte  ignoré pour une ligne {@code MB}, dont le compte est calculé (RG-DEC-12)
 * @param volumes colonnes 2 à 25 ; une colonne absente vaut 0
 */
public record SaisieLigneDsm(Long id, CleConditionnement cle, Integer millesime, String denomination, String compte,
        Map<ColonneDsm, VolumeAlcoolPur> volumes) {

    public SaisieLigneDsm {
        volumes = volumes == null ? Map.of() : volumes;
    }
}
