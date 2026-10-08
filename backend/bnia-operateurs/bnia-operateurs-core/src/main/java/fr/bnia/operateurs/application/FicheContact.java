package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.Contact;
import fr.bnia.operateurs.domain.FicheProfessionnelle;
import fr.bnia.operateurs.domain.Invitation;
import java.util.Optional;

/**
 * Un contact, sa fiche professionnelle et ce que l'écran en montre : ses désignations, qui tomberont s'il
 * est supprimé (RG-OPE-22, 23, 45), et l'invitation en attente partie de lui (RG-OPE-52).
 */
public record FicheContact(Contact contact, FicheProfessionnelle fiche, boolean contactPrincipal,
        boolean destinataireFactures, Optional<Invitation> invitationEnAttente) {
}
