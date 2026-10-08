package fr.bnia.operateurs.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.ActionJournal;
import fr.bnia.operateurs.domain.Changement;
import fr.bnia.operateurs.domain.Contact;
import fr.bnia.operateurs.domain.CoordonneesProfessionnelles;
import fr.bnia.operateurs.domain.Email;
import fr.bnia.operateurs.domain.FicheProfessionnelle;
import fr.bnia.operateurs.domain.IdentitePersonne;
import fr.bnia.operateurs.domain.Operateur;
import fr.bnia.operateurs.domain.TypeCible;
import fr.bnia.operateurs.domain.TypeFiche;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.Module;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Contacts d'un opérateur (vision UC-10, 11 ; §4.3.4 p.20-21, C.1.9 p.104-105) : personnes sans compte,
 * dont l'identité appartient à l'opérateur. Module Contacts (RG-OPE-31).
 */
@UseCase
public class GererContacts {

    private final ContactRepository contacts;
    private final FicheRepository fiches;
    private final RattachementRepository rattachements;
    private final UtilisateurRepository utilisateurs;
    private final InvitationRepository invitations;
    private final Habilitations habilitations;
    private final References references;
    private final Journaliste journaliste;
    private final Personnes personnes;

    public GererContacts(OperateurRepository operateurs, ContactRepository contacts, FicheRepository fiches,
            RattachementRepository rattachements, UtilisateurRepository utilisateurs, InvitationRepository invitations,
            JournalActions journal, PublicationEvenements publication, Habilitations habilitations,
            ReferentielsLecture referentiels, Clock horloge) {
        this.contacts = contacts;
        this.fiches = fiches;
        this.rattachements = rattachements;
        this.utilisateurs = utilisateurs;
        this.invitations = invitations;
        this.habilitations = habilitations;
        this.references = new References(referentiels);
        this.journaliste = new Journaliste(journal, utilisateurs, referentiels, horloge);
        this.personnes = new Personnes(operateurs, contacts, fiches, rattachements, utilisateurs, invitations,
                publication, journaliste);
    }

    /** RG-OPE-31 : tout membre lit les contacts de son opérateur. */
    public FicheContact consulter(Acteur acteur, long operateur, long id) {
        habilitations.exigerLecture(acteur, operateur, Module.CONTACTS);
        Contact contact = personnes.contact(operateur, id);
        FicheProfessionnelle fiche = personnes.fiche(operateur, contact.fiche());
        Operateur o = personnes.operateur(operateur);
        return new FicheContact(contact, fiche,
                o.contactPrincipal().filter(f -> f == contact.fiche()).isPresent(),
                o.destinataireFactures().filter(d -> Objects.equals(d.fiche(), contact.fiche())).isPresent(),
                invitations.enAttentePourFiche(contact.fiche()));
    }

    /** RG-OPE-21, 27, 28, 30, 34 : la fiche d'abord, puis le contact (vision UC-10). */
    public Contact creer(Acteur acteur, long operateur, SaisieContact saisie) {
        habilitations.exigerGestion(acteur, operateur, Module.CONTACTS);
        IdentitePersonne identite = new IdentitePersonne(saisie.titre(), saisie.nom(), saisie.prenom());
        Email email = Email.facultatif(saisie.email(), "RG-OPE-28");
        CoordonneesProfessionnelles coordonnees = new CoordonneesProfessionnelles(saisie.fonction(),
                saisie.telephoneProfessionnel(), saisie.telephoneSecondaire(), saisie.adresse(), saisie.codePostal(),
                saisie.ville(), saisie.pays(), saisie.centresInteret(), saisie.observation());
        references.titre(identite.titre(), null);
        references.pays(coordonnees.pays(), null);
        references.centresInteret(coordonnees.centresInteret(), java.util.Set.of());
        verifierEmailUnique(operateur, email, null);
        FicheProfessionnelle fiche = fiches.inserer(FicheProfessionnelle.creer(operateur, TypeFiche.CONTACT,
                coordonnees, journaliste.nomDe(acteur), journaliste.maintenant()));
        Contact contact = contacts.inserer(Contact.creer(operateur, identite, email, fiche.id()));
        journaliste.noter(acteur, operateur, ActionJournal.CONTACT_CREE, TypeCible.CONTACT, contact.id(),
                identite.nomComplet());
        return contact;
    }

    /** RG-OPE-21, 23, 27, 28, 34, 51 : identité et e-mail ; la fiche se modifie à part. */
    public Contact modifier(Acteur acteur, long operateur, long id, SaisieIdentiteContact saisie) {
        habilitations.exigerGestion(acteur, operateur, Module.CONTACTS);
        Contact contact = personnes.contact(operateur, id);
        IdentitePersonne identite = new IdentitePersonne(saisie.titre(), saisie.nom(), saisie.prenom());
        Email email = Email.facultatif(saisie.email(), "RG-OPE-28");
        references.titre(identite.titre(), contact.identite().titre());
        verifierEmailUnique(operateur, email, id);
        if (email == null && contact.email() != null && estDestinataireFactures(operateur, contact.fiche())) {
            throw new RegleEnfreinte("RG-OPE-23",
                    contact.identite().nomComplet() + " reçoit les factures : son e-mail est requis.");
        }
        List<Changement> changements = contact.modifier(identite, email, saisie.version());
        contacts.mettreAJour(contact);
        if (!changements.isEmpty()) {
            journaliste.noter(acteur, operateur, ActionJournal.CONTACT_MODIFIE, TypeCible.CONTACT, id,
                    identite.nomComplet(), changements);
        }
        return contact;
    }

    /**
     * RG-OPE-22, 23, 34, 45 : les invitations en attente qui partent de lui sont annulées, puis le contact
     * et sa fiche sont supprimés ; ses désignations tombent.
     */
    public void supprimer(Acteur acteur, long operateur, long id) {
        habilitations.exigerGestion(acteur, operateur, Module.CONTACTS);
        Contact contact = personnes.contact(operateur, id);
        String nom = contact.identite().nomComplet();
        personnes.annulerInvitationsDeFiche(acteur, operateur, contact.fiche(), nom);
        contacts.supprimer(id);
        personnes.supprimerFiche(operateur, contact.fiche());
        journaliste.noter(acteur, operateur, ActionJournal.CONTACT_SUPPRIME, TypeCible.CONTACT, id, nom);
    }

    /**
     * RG-OPE-28 : avertissements non bloquants pendant la saisie de l'e-mail d'un contact : celui d'un
     * membre de l'opérateur, ou d'une invitation en attente de l'opérateur. Aucune recherche ailleurs.
     *
     * @param sauf le contact modifié, ou {@code null} à la création
     */
    public List<String> avertissements(Acteur acteur, long operateur, String email, Long sauf) {
        habilitations.exigerLecture(acteur, operateur, Module.CONTACTS);
        Email adresse;
        try {
            adresse = Email.facultatif(email, "RG-OPE-28");
        } catch (RegleEnfreinte enCoursDeFrappe) {
            return List.of();
        }
        if (adresse == null) {
            return List.of();
        }
        List<String> avertissements = new ArrayList<>();
        utilisateurs.parEmail(adresse)
                .filter(u -> rattachements.de(operateur, u.id()).isPresent())
                .ifPresent(u -> avertissements.add("« " + adresse + " » est l'e-mail de " + u.identite().nomComplet()
                        + ", membre de cet opérateur."));
        invitations.enAttentePourEmail(operateur, adresse)
                .filter(i -> sauf == null || i.fiche().isEmpty()
                        || contacts.parId(sauf).map(c -> c.fiche() != i.fiche().get()).orElse(true))
                .ifPresent(i -> avertissements.add("Une invitation en attente a été envoyée à « " + adresse + " »."));
        return avertissements;
    }

    /** RG-OPE-28 : unique parmi les contacts de l'opérateur, à la casse près. */
    private void verifierEmailUnique(long operateur, Email email, Long sauf) {
        if (email != null && contacts.existeEmail(operateur, email, sauf)) {
            throw new ConflitUnicite("RG-OPE-28", "L'e-mail « " + email + " » est déjà celui d'un autre contact de"
                    + " cet opérateur.");
        }
    }

    private boolean estDestinataireFactures(long operateur, long fiche) {
        return personnes.operateur(operateur).destinataireFactures()
                .filter(d -> Objects.equals(d.fiche(), fiche)).isPresent();
    }
}
