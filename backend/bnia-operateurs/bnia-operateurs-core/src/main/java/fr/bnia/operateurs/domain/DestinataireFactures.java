package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;

/**
 * Destinataire des factures (C.8.1.1 p.123, RG-OPE-23) : la fiche professionnelle d'une personne de
 * l'opérateur, ou une adresse e-mail saisie ; jamais les deux.
 *
 * @param fiche   identifiant de la fiche professionnelle, ou {@code null}
 * @param adresse adresse saisie (RG-OPE-24), ou {@code null}
 */
public record DestinataireFactures(Long fiche, Email adresse) {

    public DestinataireFactures {
        if ((fiche == null) == (adresse == null)) {
            throw new RegleEnfreinte("RG-OPE-23",
                    "Le destinataire des factures est soit une personne de l'annuaire, soit une adresse e-mail saisie.");
        }
    }

    public static DestinataireFactures fiche(long fiche) {
        return new DestinataireFactures(fiche, null);
    }

    public static DestinataireFactures adresse(Email adresse) {
        return new DestinataireFactures(null, adresse);
    }
}
