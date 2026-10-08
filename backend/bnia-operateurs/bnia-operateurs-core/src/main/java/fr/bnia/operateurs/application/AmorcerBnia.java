package fr.bnia.operateurs.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.operateurs.domain.Invitation;
import fr.bnia.operateurs.domain.Rattachement;
import fr.bnia.operateurs.domain.partage.Role;
import java.util.Optional;

/**
 * Amorçage d'une installation : l'opérateur BNIA, créé par la migration (RG-OPE-39), n'a encore aucun
 * membre ; personne ne peut donc inviter. L'application invite elle-même un premier ADMIN, désigné par
 * la configuration, tant que le BNIA n'a ni ADMIN ni invitation en attente (choix d'implémentation du
 * 2026-10-08).
 */
@UseCase
public class AmorcerBnia {

    private final OperateurRepository operateurs;
    private final RattachementRepository rattachements;
    private final InvitationRepository invitations;
    private final GererInvitations gererInvitations;

    public AmorcerBnia(OperateurRepository operateurs, RattachementRepository rattachements,
            InvitationRepository invitations, GererInvitations gererInvitations) {
        this.operateurs = operateurs;
        this.rattachements = rattachements;
        this.invitations = invitations;
        this.gererInvitations = gererInvitations;
    }

    /** Vide si le BNIA a déjà un ADMIN actif ou une invitation en attente : il n'y a rien à amorcer. */
    public Optional<Invitation.Emise> amorcer(String email, String nom, String prenom) {
        long bnia = operateurs.idBnia();
        boolean aUnAdmin = rattachements.deLOperateur(bnia).stream().anyMatch(Rattachement::estAdminActif);
        if (aUnAdmin || !invitations.enAttente(bnia).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(gererInvitations.emettre(null, bnia, SaisieInvitation.personne(email, Role.ADMIN, nom, prenom)));
    }
}
