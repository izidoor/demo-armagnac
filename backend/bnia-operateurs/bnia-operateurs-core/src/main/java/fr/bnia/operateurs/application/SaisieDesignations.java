package fr.bnia.operateurs.application;

/**
 * Contact principal et destinataire des factures (RG-OPE-22, 23) : des fiches professionnelles de
 * l'opérateur, ou une adresse saisie pour le destinataire.
 *
 * @param contactPrincipal  identifiant de la fiche du contact principal, ou {@code null}
 * @param destinataireFiche identifiant de la fiche du destinataire des factures, ou {@code null}
 * @param destinataireEmail adresse saisie, si le destinataire n'est pas une personne de l'annuaire
 * @param version           version de l'opérateur lue par l'écran (RG-OPE-51)
 */
public record SaisieDesignations(Long contactPrincipal, Long destinataireFiche, String destinataireEmail,
        Long version) {
}
