package fr.bnia.contratsarmagnac.application;

import java.util.Set;

/**
 * Une notification push, affichée sur l'écran verrouillé : ni montant ni volume.
 *
 * @param utilisateurs destinataires, identifiants des utilisateurs dans {@code operateurs}
 * @param lien         le contrat au portail : {@code /portail/contrats/12}
 */
public record NotificationContrat(Set<Long> utilisateurs, String titre, String texte, String lien) {
}
