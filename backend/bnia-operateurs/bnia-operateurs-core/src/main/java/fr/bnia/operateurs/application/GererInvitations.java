package fr.bnia.operateurs.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.ActionJournal;
import fr.bnia.operateurs.domain.Changement;
import fr.bnia.operateurs.domain.ChampJournal;
import fr.bnia.operateurs.domain.Contact;
import fr.bnia.operateurs.domain.CoordonneesProfessionnelles;
import fr.bnia.operateurs.domain.Email;
import fr.bnia.operateurs.domain.FicheProfessionnelle;
import fr.bnia.operateurs.domain.IdentitePersonne;
import fr.bnia.operateurs.domain.Invitation;
import fr.bnia.operateurs.domain.Jeton;
import fr.bnia.operateurs.domain.Operateur;
import fr.bnia.operateurs.domain.Rattachement;
import fr.bnia.operateurs.domain.TypeCible;
import fr.bnia.operateurs.domain.TypeFiche;
import fr.bnia.operateurs.domain.Utilisateur;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.Module;
import fr.bnia.operateurs.domain.partage.UtilisateurCree;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Invitations (vision UC-01, 02, 12, 13, 20 ; spec 02 §3.2.5) : inviter, relancer, annuler, expirer, et
 * accepter, qui crée l'utilisateur et son rattachement en une transaction (RG-OPE-40 à 42). Module Accès
 * (RG-OPE-31).
 */
@UseCase
public class GererInvitations {

    private final ContactRepository contacts;
    private final FicheRepository fiches;
    private final RattachementRepository rattachements;
    private final UtilisateurRepository utilisateurs;
    private final InvitationRepository invitations;
    private final PublicationEvenements publication;
    private final Habilitations habilitations;
    private final ParametresOperateurs parametres;
    private final References references;
    private final Journaliste journaliste;
    private final Personnes personnes;
    private final Courrier courrier;

    public GererInvitations(OperateurRepository operateurs, ContactRepository contacts, FicheRepository fiches,
            RattachementRepository rattachements, UtilisateurRepository utilisateurs, InvitationRepository invitations,
            JournalActions journal, PublicationEvenements publication, EnvoiCourriels envoi, Habilitations habilitations,
            ParametresOperateurs parametres, ReferentielsLecture referentiels, Clock horloge) {
        this.contacts = contacts;
        this.fiches = fiches;
        this.rattachements = rattachements;
        this.utilisateurs = utilisateurs;
        this.invitations = invitations;
        this.publication = publication;
        this.habilitations = habilitations;
        this.parametres = parametres;
        this.references = new References(referentiels);
        this.journaliste = new Journaliste(journal, utilisateurs, referentiels, horloge);
        this.personnes = new Personnes(operateurs, contacts, fiches, rattachements, utilisateurs, invitations,
                publication, journaliste);
        this.courrier = new Courrier(envoi, parametres);
    }

    /** RG-OPE-31 : tout membre voit les invitations de son opérateur (annuaire, RG-OPE-52). */
    public Invitation consulter(Acteur acteur, long operateur, long id) {
        habilitations.exigerLecture(acteur, operateur, Module.ACCES);
        return personnes.invitation(operateur, id);
    }

    /**
     * RG-OPE-28, 29, 34, 40 : depuis un contact ou une personne nouvelle ; refusée si une invitation est
     * déjà en attente pour ce contact ou cet e-mail, ou si l'e-mail est celui d'un membre. L'e-mail part
     * après la validation de la transaction.
     *
     * @return l'invitation et son jeton en clair (jamais renvoyé par l'API : il part par e-mail)
     */
    public Invitation.Emise inviter(Acteur acteur, long operateur, SaisieInvitation saisie) {
        habilitations.exigerGestion(acteur, operateur, Module.ACCES);
        return emettre(acteur, operateur, saisie);
    }

    /**
     * RG-OPE-40 : sans contrôle de droits, réservé à la création d'un opérateur (premier ADMIN, contrôlé par
     * {@link GererOperateurs}) et l'amorçage du BNIA.
     *
     * @param acteur {@code null} : l'application
     */
    public Invitation.Emise emettre(Acteur acteur, long operateur, SaisieInvitation saisie) {
        Operateur o = personnes.operateur(operateur);
        Email email = Email.de(saisie.email(), "RG-OPE-40");
        expirerEchues(acteur, invitations.enAttente(operateur));
        Long fiche = null;
        IdentitePersonne personne = null;
        String nom;
        if (saisie.contact() != null) {
            Contact contact = personnes.contact(operateur, saisie.contact());
            nom = contact.identite().nomComplet();
            if (invitations.enAttentePourFiche(contact.fiche()).isPresent()) {
                throw new ConflitUnicite("RG-OPE-40", "Une invitation est déjà en attente pour " + nom
                        + " : relancez-la.");
            }
            mettreAJourEmailDuContact(acteur, contact, email);
            fiche = contact.fiche();
        } else {
            personne = new IdentitePersonne(saisie.titre(), saisie.nom(), saisie.prenom());
            references.titre(personne.titre(), null);
            nom = personne.nomComplet();
        }
        if (invitations.enAttentePourEmail(operateur, email).isPresent()) {
            throw new ConflitUnicite("RG-OPE-40", "Une invitation est déjà en attente pour « " + email
                    + " » dans cet opérateur : relancez-la.");
        }
        Optional<Utilisateur> titulaire = utilisateurs.parEmail(email);
        titulaire.filter(u -> rattachements.de(operateur, u.id()).isPresent()).ifPresent(u -> {
            throw new ConflitUnicite("RG-OPE-40", "« " + email + " » est l'e-mail de " + u.identite().nomComplet()
                    + ", déjà membre de cet opérateur.");
        });
        String auteur = journaliste.nomDe(acteur);
        Invitation.Emise emise = Invitation.emettre(operateur, email, saisie.role(), fiche, personne, saisie.fonction(),
                saisie.telephoneProfessionnel(), auteur, journaliste.maintenant(), parametres.validiteInvitation(),
                titulaire.map(Utilisateur::id).orElse(null));
        Invitation invitation = invitations.inserer(emise.invitation());
        journaliste.noter(acteur, operateur, ActionJournal.INVITATION_ENVOYEE, TypeCible.INVITATION, invitation.id(),
                nom, List.of(new Changement(ChampJournal.ROLE, null, invitation.role().libelle())));
        courrier.invitation(email.valeur(), o.identite().raisonSociale(), invitation.role(), emise.jeton(),
                invitation.expireLe(), auteur);
        return new Invitation.Emise(invitation, emise.jeton());
    }

    /** RG-OPE-34, 41 : nouveau jeton, nouvelle expiration, nouvel e-mail. */
    public Invitation.Emise relancer(Acteur acteur, long operateur, long id) {
        habilitations.exigerGestion(acteur, operateur, Module.ACCES);
        Invitation invitation = personnes.invitation(operateur, id);
        String jeton = invitation.relancer(journaliste.maintenant(), parametres.validiteInvitation());
        invitations.mettreAJour(invitation);
        String auteur = journaliste.nomDe(acteur);
        journaliste.noter(acteur, operateur, ActionJournal.INVITATION_RELANCEE, TypeCible.INVITATION, id,
                nomInvite(invitation));
        courrier.invitation(invitation.email().valeur(), personnes.operateur(operateur).identite().raisonSociale(),
                invitation.role(), jeton, invitation.expireLe(), auteur);
        return new Invitation.Emise(invitation, jeton);
    }

    /** RG-OPE-34, 41 : le contact dont elle partait reste inchangé. */
    public void annuler(Acteur acteur, long operateur, long id) {
        habilitations.exigerGestion(acteur, operateur, Module.ACCES);
        Invitation invitation = personnes.invitation(operateur, id);
        invitation.annuler();
        invitations.mettreAJour(invitation);
        journaliste.noter(acteur, operateur, ActionJournal.INVITATION_ANNULEE, TypeCible.INVITATION, id,
                nomInvite(invitation));
    }

    /** RG-OPE-41 : les invitations en attente dont l'expiration est passée deviennent {@code EXPIREE}. */
    public int expirerEchues() {
        List<Invitation> echues = invitations.echues(journaliste.maintenant());
        expirerEchues(null, echues);
        return echues.size();
    }

    /** Ce que montre le lien d'une invitation (vision UC-20) : refusé si elle n'est plus acceptable. */
    public InvitationPublique consulterParJeton(String jeton) {
        Invitation invitation = parJeton(jeton);
        invitation.verifierAcceptable(journaliste.maintenant());
        Optional<Utilisateur> titulaire = utilisateurs.parEmail(invitation.email());
        invitation.verifierCompteVise(titulaire.map(Utilisateur::id).orElse(null));
        Operateur operateur = personnes.operateur(invitation.operateur());
        IdentitePersonne identite = identiteProposee(invitation);
        return new InvitationPublique(operateur.identite().raisonSociale(), invitation.role(),
                invitation.email().valeur(), identite.titre(), identite.nom(), identite.prenom(),
                titulaire.isPresent(), invitation.expireLe());
    }

    /**
     * RG-OPE-29, 30, 34, 42, 49 : en une transaction, trouver ou créer l'utilisateur ({@code authentification}
     * crée alors son compte, sans mot de passe), refuser s'il est déjà membre, reprendre la fiche du contact
     * invité (le contact est supprimé) ou en créer une, créer le rattachement {@code ACTIF}, passer
     * l'invitation à {@code ACCEPTEE}.
     *
     * @param connecte l'utilisateur connecté, exigé si l'e-mail de l'invitation est déjà celui d'un compte
     */
    public Acceptation accepter(String jeton, Optional<Acteur> connecte, SaisieAcceptation saisie) {
        Invitation invitation = parJeton(jeton);
        Instant maintenant = journaliste.maintenant();
        invitation.verifierAcceptable(maintenant);
        long operateur = invitation.operateur();
        Optional<Utilisateur> existant = utilisateurs.parEmail(invitation.email());
        invitation.verifierCompteVise(existant.map(Utilisateur::id).orElse(null));
        Utilisateur utilisateur;
        boolean nouveau = existant.isEmpty();
        if (existant.isPresent()) {
            utilisateur = existant.get();
            if (connecte.map(a -> a.utilisateur() != utilisateur.id()).orElse(true)) {
                throw new AccesRefuse("RG-OPE-42", "Un compte existe déjà pour « " + invitation.email()
                        + " » : connectez-vous avec ce compte pour accepter l'invitation.");
            }
            if (rattachements.de(operateur, utilisateur.id()).isPresent()) {
                throw new RegleEnfreinte("RG-OPE-42", "Vous êtes déjà membre de cet opérateur.");
            }
            utilisateur.proposerOperateurActif(operateur);
            utilisateurs.mettreAJour(utilisateur);
        } else {
            IdentitePersonne identite = IdentitePersonne.utilisateur(saisie.titre(), saisie.nom(), saisie.prenom());
            references.titre(identite.titre(), identiteProposee(invitation).titre());
            utilisateur = utilisateurs.inserer(Utilisateur.creer(identite, invitation.email(), operateur));
            publication.publier(new UtilisateurCree(utilisateur.id(), utilisateur.email().valeur()));
        }
        long fiche = ficheDuMembre(invitation, utilisateur);
        Rattachement rattachement = rattachements.inserer(Rattachement.creer(operateur, utilisateur.id(),
                invitation.role(), fiche, maintenant));
        invitation.accepter(utilisateur.id(), maintenant);
        invitations.mettreAJour(invitation);
        Acteur acteur = new Acteur(utilisateur.id());
        journaliste.noter(acteur, operateur, ActionJournal.INVITATION_ACCEPTEE, TypeCible.MEMBRE, rattachement.id(),
                utilisateur.identite().nomComplet(),
                List.of(new Changement(ChampJournal.ROLE, null, invitation.role().libelle())));
        return new Acceptation(utilisateur.id(), operateur, rattachement.id(), nouveau);
    }

    /**
     * RG-AUT-15 : l'utilisateur qui a accepté l'invitation de ce jeton il y a moins d'une heure ; lu par
     * {@code authentification} pour poser le premier mot de passe.
     */
    public Optional<Long> utilisateurDInvitationAcceptee(String jeton) {
        return invitations.parEmpreinte(Jeton.empreinte(jeton))
                .flatMap(i -> i.accepteeRecemmentPar(journaliste.maintenant(), parametres.delaiPremierMotDePasse()));
    }

    /** RG-OPE-30 : la fiche du contact invité change de porteur (le contact est supprimé), ou une fiche est créée. */
    private long ficheDuMembre(Invitation invitation, Utilisateur utilisateur) {
        if (invitation.fiche().isPresent()) {
            long id = invitation.fiche().get();
            contacts.parFiche(id).ifPresent(contact -> contacts.supprimer(contact.id()));
            FicheProfessionnelle fiche = fiches.parId(id).orElseThrow(() -> new ElementIntrouvable(
                    "La fiche du contact invité n'existe plus : demandez une nouvelle invitation."));
            fiche.porterPar(TypeFiche.RATTACHEMENT);
            fiches.mettreAJour(fiche);
            return id;
        }
        return fiches.inserer(FicheProfessionnelle.creer(invitation.operateur(), TypeFiche.RATTACHEMENT,
                CoordonneesProfessionnelles.de(invitation.fonction(), invitation.telephoneProfessionnel()),
                utilisateur.identite().nomComplet(), journaliste.maintenant())).id();
    }

    /** RG-OPE-40 : partant d'un contact, son e-mail est mis à jour s'il diffère (RG-OPE-28 : unique). */
    private void mettreAJourEmailDuContact(Acteur acteur, Contact contact, Email email) {
        String avant = contact.email() == null ? null : contact.email().valeur();
        if (contact.aEmail(email)) {
            return;
        }
        if (contacts.existeEmail(contact.operateur(), email, contact.id())) {
            throw new ConflitUnicite("RG-OPE-28", "L'e-mail « " + email + " » est déjà celui d'un autre contact de"
                    + " cet opérateur.");
        }
        contact.mettreAJourEmail(email);
        contacts.mettreAJour(contact);
        List<Changement> changements = new ArrayList<>();
        changements.add(new Changement(ChampJournal.EMAIL, avant, email.valeur()));
        journaliste.noter(acteur, contact.operateur(), ActionJournal.CONTACT_MODIFIE, TypeCible.CONTACT, contact.id(),
                contact.identite().nomComplet(), changements);
    }

    private void expirerEchues(Acteur acteur, List<Invitation> candidates) {
        Instant maintenant = journaliste.maintenant();
        for (Invitation invitation : candidates) {
            if (invitation.expirerSiEchue(maintenant)) {
                invitations.mettreAJour(invitation);
                journaliste.noter(acteur, invitation.operateur(), ActionJournal.INVITATION_EXPIREE,
                        TypeCible.INVITATION, invitation.id(), nomInvite(invitation));
            }
        }
    }

    private Invitation parJeton(String jeton) {
        return invitations.parEmpreinte(Jeton.empreinte(jeton == null ? "" : jeton))
                .orElseThrow(() -> new ElementIntrouvable("Ce lien d'invitation n'est pas valable : il a peut-être été"
                        + " remplacé par une relance. Utilisez le dernier lien reçu."));
    }

    /** Identité préremplie : celle du contact invité, ou celle saisie pour la personne nouvelle. */
    private IdentitePersonne identiteProposee(Invitation invitation) {
        return invitation.personne()
                .or(() -> invitation.fiche().flatMap(contacts::parFiche).map(Contact::identite))
                .orElse(new IdentitePersonne(null, invitation.email().valeur(), null));
    }

    private String nomInvite(Invitation invitation) {
        return identiteProposee(invitation).nomComplet();
    }
}
