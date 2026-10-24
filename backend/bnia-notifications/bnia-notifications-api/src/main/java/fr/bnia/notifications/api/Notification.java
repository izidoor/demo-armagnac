package fr.bnia.notifications.api;

import java.util.Objects;
import java.util.Set;

/**
 * Une notification du système (spec 11) : le contexte métier décide de ses destinataires et de son texte, affiché sur
 * l'écran verrouillé de leurs appareils, donc sans montant ni volume.
 *
 * @param utilisateurs destinataires, identifiants des utilisateurs dans {@code operateurs}
 * @param titre        une ligne : « Projet de contrat CA-2026-0012 à approuver »
 * @param texte        une phrase
 * @param lien         chemin de l'application ouvert par un appui : {@code /portail/contrats/12}
 */
public record Notification(Set<Long> utilisateurs, String titre, String texte, String lien) {

    public Notification {
        utilisateurs = Set.copyOf(utilisateurs);
        Objects.requireNonNull(titre);
        Objects.requireNonNull(texte);
        Objects.requireNonNull(lien);
    }
}
