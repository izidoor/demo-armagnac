package fr.bnia.operateurs.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.operateurs.domain.ActionJournal;
import fr.bnia.operateurs.domain.Changement;
import fr.bnia.operateurs.domain.ChampJournal;
import fr.bnia.operateurs.domain.Contact;
import fr.bnia.operateurs.domain.DernierAdmin;
import fr.bnia.operateurs.domain.FicheProfessionnelle;
import fr.bnia.operateurs.domain.Operateur;
import fr.bnia.operateurs.domain.Rattachement;
import fr.bnia.operateurs.domain.StatutRattachement;
import fr.bnia.operateurs.domain.TypeCible;
import fr.bnia.operateurs.domain.TypeFiche;
import fr.bnia.operateurs.domain.Utilisateur;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.Module;
import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Membres d'un opérateur (vision UC-14, 15, 16) : changer le rôle, suspendre, réactiver, retirer
 * l'accès. Module Accès (RG-OPE-31) ; le dernier ADMIN est protégé, sous le verrou de l'opérateur
 * (RG-OPE-35, A-OPE-6).
 */
@UseCase
public class GererMembres {

    private final OperateurRepository operateurs;
    private final ContactRepository contacts;
    private final FicheRepository fiches;
    private final RattachementRepository rattachements;
    private final Habilitations habilitations;
    private final Journaliste journaliste;
    private final Personnes personnes;

    public GererMembres(OperateurRepository operateurs, ContactRepository contacts, FicheRepository fiches,
            RattachementRepository rattachements, UtilisateurRepository utilisateurs, InvitationRepository invitations,
            JournalActions journal, PublicationEvenements publication, Habilitations habilitations,
            ReferentielsLecture referentiels, Clock horloge) {
        this.operateurs = operateurs;
        this.contacts = contacts;
        this.fiches = fiches;
        this.rattachements = rattachements;
        this.habilitations = habilitations;
        this.journaliste = new Journaliste(journal, utilisateurs, referentiels, horloge);
        this.personnes = new Personnes(operateurs, contacts, fiches, rattachements, utilisateurs, invitations,
                publication, journaliste);
    }

    /** RG-OPE-31 : tout membre lit les membres de son opérateur. */
    public FicheMembre consulter(Acteur acteur, long operateur, long id) {
        habilitations.exigerLecture(acteur, operateur, Module.CONTACTS);
        Rattachement rattachement = personnes.membre(operateur, id);
        Operateur o = personnes.operateur(operateur);
        long fiche = rattachement.fiche();
        return new FicheMembre(rattachement, personnes.utilisateur(rattachement.utilisateur()),
                personnes.fiche(operateur, fiche), o.contactPrincipal().filter(f -> f == fiche).isPresent(),
                o.destinataireFactures().filter(d -> Objects.equals(d.fiche(), fiche)).isPresent(),
                rattachement.estAdminActif() && adminsActifs(operateur) == 1);
    }

    /** RG-OPE-34, 35, 43 : effet immédiat, pour cet opérateur seul. */
    public Rattachement changerRole(Acteur acteur, long operateur, long id, Role role) {
        Droits droits = habilitations.exigerGestion(acteur, operateur, Module.ACCES);
        operateurs.verrouiller(operateur);
        Rattachement rattachement = personnes.membre(operateur, id);
        String nom = nom(rattachement);
        Role avant = rattachement.role();
        if (role != Role.ADMIN) {
            verifierDernierAdmin(rattachement, droits, nom);
        }
        rattachement.changerRole(role);
        rattachements.mettreAJour(rattachement);
        journaliste.noter(acteur, operateur, ActionJournal.ROLE_CHANGE, TypeCible.MEMBRE, id, nom,
                List.of(new Changement(ChampJournal.ROLE, avant.libelle(), rattachement.role().libelle())));
        return rattachement;
    }

    /** RG-OPE-34, 35, 43. */
    public Rattachement suspendre(Acteur acteur, long operateur, long id) {
        Droits droits = habilitations.exigerGestion(acteur, operateur, Module.ACCES);
        operateurs.verrouiller(operateur);
        Rattachement rattachement = personnes.membre(operateur, id);
        String nom = nom(rattachement);
        verifierDernierAdmin(rattachement, droits, nom);
        rattachement.suspendre();
        rattachements.mettreAJour(rattachement);
        journaliste.noter(acteur, operateur, ActionJournal.ACCES_SUSPENDU, TypeCible.MEMBRE, id, nom,
                List.of(statut(StatutRattachement.ACTIF, StatutRattachement.SUSPENDU)));
        return rattachement;
    }

    /** RG-OPE-34, 43. */
    public Rattachement reactiver(Acteur acteur, long operateur, long id) {
        habilitations.exigerGestion(acteur, operateur, Module.ACCES);
        Rattachement rattachement = personnes.membre(operateur, id);
        rattachement.reactiver();
        rattachements.mettreAJour(rattachement);
        journaliste.noter(acteur, operateur, ActionJournal.ACCES_REACTIVE, TypeCible.MEMBRE, id, nom(rattachement),
                List.of(statut(StatutRattachement.SUSPENDU, StatutRattachement.ACTIF)));
        return rattachement;
    }

    /**
     * RG-OPE-22, 23, 30, 34, 35, 44 : le rattachement est supprimé. Avec {@code conserverContact}, un contact
     * reprend une copie de l'identité et de l'e-mail, et la fiche repasse au type {@code CONTACT} ; sinon
     * la fiche est supprimée et ses désignations tombent. Un utilisateur sans autre rattachement est
     * supprimé avec son compte de connexion. Un ADMIN peut se retirer s'il n'est pas le dernier.
     *
     * @return le contact créé, si la personne est conservée
     */
    public Optional<Contact> retirer(Acteur acteur, long operateur, long id, boolean conserverContact) {
        Droits droits = habilitations.exigerGestion(acteur, operateur, Module.ACCES);
        operateurs.verrouiller(operateur);
        Rattachement rattachement = personnes.membre(operateur, id);
        Utilisateur utilisateur = personnes.utilisateur(rattachement.utilisateur());
        String nom = utilisateur.identite().nomComplet();
        verifierDernierAdmin(rattachement, droits, nom);
        rattachements.supprimer(id);
        Optional<Contact> contact = Optional.empty();
        if (conserverContact) {
            if (contacts.existeEmail(operateur, utilisateur.email(), null)) {
                throw new ConflitUnicite("RG-OPE-28", "Un contact de cet opérateur a déjà l'e-mail « "
                        + utilisateur.email() + " » : retirez l'accès sans conserver la personne, ou modifiez d'abord"
                        + " ce contact.");
            }
            FicheProfessionnelle fiche = personnes.fiche(operateur, rattachement.fiche());
            fiche.porterPar(TypeFiche.CONTACT);
            fiches.mettreAJour(fiche);
            contact = Optional.of(contacts.inserer(Contact.creer(operateur, utilisateur.identite(),
                    utilisateur.email(), fiche.id())));
        } else {
            personnes.supprimerFiche(operateur, rattachement.fiche());
        }
        journaliste.noter(acteur, operateur, ActionJournal.ACCES_RETIRE, TypeCible.MEMBRE, id, nom,
                List.of(new Changement(ChampJournal.ROLE, rattachement.role().libelle(), null)));
        contact.ifPresent(c -> journaliste.noter(acteur, operateur, ActionJournal.CONTACT_CREE, TypeCible.CONTACT,
                c.id(), nom));
        personnes.supprimerUtilisateurSiSansRattachement(acteur, utilisateur.id());
        return contact;
    }

    /** RG-OPE-35, sous le verrou de l'opérateur. */
    private void verifierDernierAdmin(Rattachement rattachement, Droits droits, String nom) {
        DernierAdmin.verifier(rattachement, adminsActifs(rattachement.operateur()), droits,
                rattachement.operateur() == operateurs.idBnia(), nom);
    }

    private long adminsActifs(long operateur) {
        return rattachements.deLOperateur(operateur).stream().filter(Rattachement::estAdminActif).count();
    }

    private String nom(Rattachement rattachement) {
        return personnes.utilisateur(rattachement.utilisateur()).identite().nomComplet();
    }

    private static Changement statut(StatutRattachement avant, StatutRattachement apres) {
        return new Changement(ChampJournal.STATUT, avant.name(), apres.name());
    }
}
