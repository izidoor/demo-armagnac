package fr.bnia.operateurs.application;

/**
 * Contact principal et destinataire des factures (RG-OPE-22, 23).
 *
 * @param contactPrincipal          identifiant d'un contact, ou {@code null}
 * @param destinataireContact       identifiant d'un contact, ou {@code null}
 * @param destinataireEmail         adresse saisie, si le destinataire n'est pas un contact
 */
public record SaisieDesignations(Long contactPrincipal, Long destinataireContact, String destinataireEmail) {
}
