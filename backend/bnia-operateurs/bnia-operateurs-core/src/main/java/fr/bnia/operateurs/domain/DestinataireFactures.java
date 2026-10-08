package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;

/**
 * Destinataire des factures (C.8.1.1 p.123, RG-OPE-23) : un contact de l'opérateur, ou une adresse
 * e-mail saisie ; jamais les deux.
 *
 * @param contact identifiant du contact, ou {@code null}
 * @param adresse adresse saisie (RG-OPE-24), ou {@code null}
 */
public record DestinataireFactures(Long contact, Email adresse) {

    public DestinataireFactures {
        if ((contact == null) == (adresse == null)) {
            throw new RegleEnfreinte("RG-OPE-23",
                    "Le destinataire des factures est soit un contact, soit une adresse e-mail saisie.");
        }
    }

    public static DestinataireFactures contact(long contact) {
        return new DestinataireFactures(contact, null);
    }

    public static DestinataireFactures adresse(Email adresse) {
        return new DestinataireFactures(null, adresse);
    }
}
