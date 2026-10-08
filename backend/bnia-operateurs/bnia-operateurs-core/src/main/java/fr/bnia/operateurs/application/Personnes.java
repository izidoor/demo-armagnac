package fr.bnia.operateurs.application;

import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.operateurs.domain.ActionJournal;
import fr.bnia.operateurs.domain.Contact;
import fr.bnia.operateurs.domain.Email;
import fr.bnia.operateurs.domain.FicheProfessionnelle;
import fr.bnia.operateurs.domain.Invitation;
import fr.bnia.operateurs.domain.Operateur;
import fr.bnia.operateurs.domain.Rattachement;
import fr.bnia.operateurs.domain.TypeCible;
import fr.bnia.operateurs.domain.Utilisateur;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.UtilisateurSupprime;
import java.util.Optional;

/**
 * Lectures et effets communs aux cas d'usage sur les personnes et les accès : retrouver un objet dans son
 * opérateur, nommer le porteur d'une fiche, faire tomber les désignations d'une fiche supprimée
 * (RG-OPE-22, 23), supprimer un utilisateur sans rattachement (RG-OPE-44).
 */
final class Personnes {

    private final OperateurRepository operateurs;
    private final ContactRepository contacts;
    private final FicheRepository fiches;
    private final RattachementRepository rattachements;
    private final UtilisateurRepository utilisateurs;
    private final InvitationRepository invitations;
    private final PublicationEvenements publication;
    private final Journaliste journaliste;

    Personnes(OperateurRepository operateurs, ContactRepository contacts, FicheRepository fiches,
            RattachementRepository rattachements, UtilisateurRepository utilisateurs, InvitationRepository invitations,
            PublicationEvenements publication, Journaliste journaliste) {
        this.operateurs = operateurs;
        this.contacts = contacts;
        this.fiches = fiches;
        this.rattachements = rattachements;
        this.utilisateurs = utilisateurs;
        this.invitations = invitations;
        this.publication = publication;
        this.journaliste = journaliste;
    }

    Operateur operateur(long id) {
        return operateurs.parId(id).orElseThrow(() -> new ElementIntrouvable("Opérateur " + id + " introuvable."));
    }

    Contact contact(long operateur, long id) {
        return contacts.parId(id).filter(c -> c.operateur() == operateur)
                .orElseThrow(() -> new ElementIntrouvable("Contact " + id + " introuvable dans cet opérateur."));
    }

    Rattachement membre(long operateur, long id) {
        return rattachements.parId(id).filter(r -> r.operateur() == operateur)
                .orElseThrow(() -> new ElementIntrouvable("Membre " + id + " introuvable dans cet opérateur."));
    }

    FicheProfessionnelle fiche(long operateur, long id) {
        return fiches.parId(id).filter(f -> f.operateur() == operateur)
                .orElseThrow(() -> new ElementIntrouvable("Fiche professionnelle " + id + " introuvable dans cet opérateur."));
    }

    Invitation invitation(long operateur, long id) {
        return invitations.parId(id).filter(i -> i.operateur() == operateur)
                .orElseThrow(() -> new ElementIntrouvable("Invitation " + id + " introuvable dans cet opérateur."));
    }

    Utilisateur utilisateur(long id) {
        return utilisateurs.parId(id).orElseThrow(() -> new ElementIntrouvable("Utilisateur " + id + " introuvable."));
    }

    /** RG-OPE-30 : le porteur d'une fiche, contact ou membre ; son nom est lu chez son propriétaire (vision principe 4). */
    String nomDeFiche(long fiche) {
        return contacts.parFiche(fiche).map(c -> c.identite().nomComplet())
                .or(() -> rattachements.parFiche(fiche).flatMap(r -> utilisateurs.parId(r.utilisateur()))
                        .map(u -> u.identite().nomComplet()))
                .orElse("Fiche n° " + fiche);
    }

    /** RG-OPE-23 : l'e-mail du porteur : celui du contact, ou celui de l'utilisateur pour un membre. */
    Optional<Email> emailDeFiche(long fiche) {
        Optional<Contact> contact = contacts.parFiche(fiche);
        if (contact.isPresent()) {
            return Optional.ofNullable(contact.get().email());
        }
        return rattachements.parFiche(fiche).flatMap(r -> utilisateurs.parId(r.utilisateur())).map(Utilisateur::email);
    }

    /** RG-OPE-22, 23 : la fiche est supprimée, ses désignations tombent. */
    void supprimerFiche(long operateur, long fiche) {
        Operateur o = operateur(operateur);
        if (o.oublierFiche(fiche)) {
            operateurs.mettreAJour(o);
        }
        fiches.supprimer(fiche);
    }

    /** RG-OPE-45 : les invitations en attente qui partent de ce contact (sa fiche) sont annulées. */
    void annulerInvitationsDeFiche(Acteur acteur, long operateur, long fiche, String nom) {
        invitations.enAttentePourFiche(fiche).ifPresent(invitation -> {
            invitation.annuler();
            invitations.mettreAJour(invitation);
            journaliste.noter(acteur, operateur, ActionJournal.INVITATION_ANNULEE, TypeCible.INVITATION,
                    invitation.id(), nom);
        });
    }

    /**
     * RG-OPE-25, 44 : un utilisateur qui n'a plus aucun rattachement est supprimé ; {@code authentification}
     * supprime son compte de connexion dans la même transaction (RG-AUT-11).
     */
    void supprimerUtilisateurSiSansRattachement(Acteur acteur, long utilisateur) {
        if (!rattachements.deLUtilisateur(utilisateur).isEmpty()) {
            return;
        }
        utilisateurs.parId(utilisateur).ifPresent(u -> {
            utilisateurs.supprimer(utilisateur);
            journaliste.noter(acteur, null, ActionJournal.UTILISATEUR_SUPPRIME, TypeCible.UTILISATEUR, utilisateur,
                    u.identite().nomComplet());
            publication.publier(new UtilisateurSupprime(utilisateur));
        });
    }
}
