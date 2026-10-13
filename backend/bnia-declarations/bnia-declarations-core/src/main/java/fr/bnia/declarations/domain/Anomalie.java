package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.VolumeAlcoolPur;

/**
 * Une anomalie de calcul (RG-DEC-18), montrée dès la saisie avec son niveau et son écart. Elle n'empêche
 * pas d'enregistrer le brouillon ; elle bloque l'envoi (lot 2).
 *
 * @param onglet  onglet où la montrer
 * @param colonne colonne concernée, {@code null} si aucune
 * @param ligne   rang de la ligne dans la DSM (0 pour la première), {@code null} au niveau de l'onglet ou de la DSM
 * @param ecart   volume en cause (stock négatif, différence des colonnes croisées), {@code null} sinon
 */
public record Anomalie(TypeAnomalie type, NiveauAnomalie niveau, OngletDsm onglet, ColonneDsm colonne, Integer ligne,
        String message, VolumeAlcoolPur ecart) {
}
