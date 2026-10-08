package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.StatutRattachement;
import fr.bnia.operateurs.domain.StatutUtilisateur;
import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.util.List;

/**
 * Ligne de la liste des utilisateurs du back-office (RG-OPE-53). Le blocage temporaire et la dernière
 * connexion sont lus dans {@code authentification} par l'écran.
 */
public record UtilisateurLigne(long id, String nom, String prenom, String email, StatutUtilisateur statut,
        List<RattachementLigne> rattachements) {

    /**
     * Un rattachement de l'utilisateur, avec la raison sociale de l'opérateur.
     *
     * @param type                le type BNIA ouvre le back-office, les autres le portail (RG-OPE-49)
     * @param operateurEnActivite faux si l'opérateur est désactivé : le rattachement est gelé (RG-OPE-26)
     */
    public record RattachementLigne(long id, long operateur, String code, String raisonSociale, TypeOperateur type, Role role,
            StatutRattachement statut, boolean operateurEnActivite) {
    }
}
