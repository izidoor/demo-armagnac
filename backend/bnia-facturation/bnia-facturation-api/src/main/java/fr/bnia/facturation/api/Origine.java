package fr.bnia.facturation.api;

import fr.bnia.commun.domain.RegleEnfreinte;

/**
 * Ce qu'une facture facture, fourni par la source (spec 08 §3.4) ; remplace le type de facture du CDC. Opaque
 * pour {@code facturation} : sans clé étrangère (A-FAC-8), rendu tel quel dans les événements (RG-FAC-25).
 *
 * @param source        nom de la source : {@code declarations}, plus tard {@code distillation}, {@code boutique}
 * @param nature        code stable, lu par {@code statistiques} : {@code DSM}
 * @param libelleNature titre du PDF et filtre de l'historique : « Cotisation interprofessionnelle Armagnac »
 * @param identifiant   identifiant de l'objet chez la source (identifiant de la DSM)
 * @param reference     affichée : « DSM juillet 2026 »
 */
public record Origine(String source, String nature, String libelleNature, String identifiant, String reference) {

    public Origine {
        source = obligatoire(source, "la source");
        nature = obligatoire(nature, "la nature");
        libelleNature = obligatoire(libelleNature, "le libellé de la nature");
        identifiant = obligatoire(identifiant, "l'identifiant");
        reference = obligatoire(reference, "la référence");
    }

    private static String obligatoire(String valeur, String nom) {
        if (valeur == null || valeur.isBlank()) {
            throw new RegleEnfreinte("RG-FAC-01", "L'origine d'une facture a " + nom + ".");
        }
        return valeur.strip();
    }
}
