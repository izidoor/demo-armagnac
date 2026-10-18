package fr.bnia.operateurs.application;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.bnia.operateurs.domain.Adresse;
import fr.bnia.operateurs.domain.CodeOperateur;
import fr.bnia.operateurs.domain.Contact;
import fr.bnia.operateurs.domain.CoordonneesProfessionnelles;
import fr.bnia.operateurs.domain.Declaratif;
import fr.bnia.operateurs.domain.DiffusionMillesimes;
import fr.bnia.operateurs.domain.Email;
import fr.bnia.operateurs.domain.FicheProfessionnelle;
import fr.bnia.operateurs.domain.IdentiteOperateur;
import fr.bnia.operateurs.domain.IdentitePersonne;
import fr.bnia.operateurs.domain.Invitation;
import fr.bnia.operateurs.domain.Operateur;
import fr.bnia.operateurs.domain.Production;
import fr.bnia.operateurs.domain.Rattachement;
import fr.bnia.operateurs.domain.StatutInvitation;
import fr.bnia.operateurs.domain.TypeCible;
import fr.bnia.operateurs.domain.TypeFiche;
import fr.bnia.operateurs.domain.Utilisateur;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import fr.bnia.referentiels.domain.partage.LangueCertificat;
import fr.bnia.referentiels.domain.partage.NiveauDetailCertificat;
import fr.bnia.referentiels.domain.partage.PaysVue;
import fr.bnia.referentiels.domain.partage.ValeurVue;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Le contexte en mémoire, pour les tests de cas d'usage : un dépôt par agrégat qui attribue les
 * identifiants et incrémente les versions, le journal, les e-mails et les événements retenus, une
 * horloge réglable. L'opérateur BNIA existe d'emblée (RG-OPE-39), comme après la migration.
 */
final class Memoire {

    static final long FRANCE = 1;
    static final long TITRE = 10;

    final Horloge horloge = new Horloge(Instant.parse("2026-10-08T08:00:00Z"));
    final OperateursEnMemoire operateurs = new OperateursEnMemoire();
    final Contacts contacts = new Contacts();
    final Fiches fiches = new Fiches();
    final Utilisateurs utilisateurs = new Utilisateurs();
    final Rattachements rattachements = new Rattachements();
    final Invitations invitations = new Invitations();
    final Journal journal = new Journal();
    final Courriels courriels = new Courriels();
    final EvenementsCaptures evenements = new EvenementsCaptures();
    final ReferentielsLecture referentiels = mock(ReferentielsLecture.class);
    final ParametresOperateurs parametres = new ParametresOperateurs("https://bnia.example/", Duration.ofDays(7),
            Duration.ofHours(24), Duration.ofHours(1));
    final long bnia;

    Memoire() {
        PaysVue france = new PaysVue(FRANCE, "FR", "FR", "France", 1, true, LangueCertificat.FR_EN,
                NiveauDetailCertificat.CUMUL_DEGRE, Optional.empty(), true);
        when(referentiels.pays(FRANCE)).thenReturn(Optional.of(france));
        when(referentiels.paysActifs()).thenReturn(List.of(france));
        when(referentiels.titre(TITRE)).thenReturn(Optional.of(new ValeurVue(TITRE, "MME", "Madame", true)));
        bnia = operateurs.inserer(Operateur.reconstituer(null, TypeOperateur.BNIA, CodeOperateur.de("BNIA"),
                identite("BNIA"), Declaratif.parDefaut(TypeOperateur.BNIA), Production.aucune(),
                DiffusionMillesimes.refus(), false, true, null, null, 0)).id();
        operateurs.cascade = id -> {
            rattachements.lignes.values().removeIf(r -> r.operateur() == id);
            contacts.lignes.values().removeIf(c -> c.operateur() == id);
            fiches.lignes.values().removeIf(f -> f.operateur() == id);
            invitations.lignes.values().removeIf(i -> i.operateur() == id);
        };
    }

    // Cas d'usage, câblés comme par Spring

    Habilitations habilitations() {
        return new Habilitations(utilisateurs, rattachements, operateurs);
    }

    GererInvitations gererInvitations() {
        return new GererInvitations(operateurs, contacts(), fiches(), rattachements, utilisateurs, invitations, journal,
                evenements, courriels, habilitations(), parametres, referentiels, horloge);
    }

    GererOperateurs gererOperateurs() {
        return new GererOperateurs(operateurs, contacts(), fiches(), rattachements, utilisateurs, invitations, operateurs,
                operateurs, referentiels, mock(OperateursLecture.class), journal, evenements, habilitations(),
                gererInvitations(), horloge);
    }

    GererContacts gererContacts() {
        return new GererContacts(operateurs, contacts(), fiches(), rattachements, utilisateurs, invitations, journal,
                evenements, habilitations(), referentiels, horloge);
    }

    GererFiches gererFiches() {
        return new GererFiches(operateurs, contacts(), fiches(), rattachements, utilisateurs, invitations, journal,
                evenements, habilitations(), referentiels, horloge);
    }

    GererMembres gererMembres() {
        return new GererMembres(operateurs, contacts(), fiches(), rattachements, utilisateurs, invitations, journal,
                evenements, habilitations(), referentiels, horloge);
    }

    GererUtilisateurs gererUtilisateurs() {
        return new GererUtilisateurs(utilisateurs, rattachements, lecture(), journal, courriels, habilitations(),
                parametres, referentiels, horloge);
    }

    MonCompte monCompte() {
        return new MonCompte(utilisateurs, lecture(), journal, evenements, courriels, parametres, referentiels, horloge);
    }

    GererPresentation gererPresentation() {
        return new GererPresentation(new PresentationsEnMemoire(), operateurs, referentiels, habilitations());
    }

    ContactRepository contacts() {
        return contacts;
    }

    FicheRepository fiches() {
        return fiches;
    }

    /** Lecture des utilisateurs et de leurs rattachements, comme le SQL de l'adapter. */
    UtilisateursLecture lecture() {
        return new UtilisateursLecture() {
            @Override
            public List<UtilisateurLigne> lister(FiltreUtilisateurs filtre) {
                return utilisateurs.lignes.values().stream().map(this::ligne).toList();
            }

            @Override
            public Optional<UtilisateurLigne> utilisateur(long id) {
                return utilisateurs.parId(id).map(this::ligne);
            }

            private UtilisateurLigne ligne(Utilisateur u) {
                return new UtilisateurLigne(u.id(), u.identite().nom(), u.identite().prenom(), u.email().valeur(),
                        u.statut(), rattachements.deLUtilisateur(u.id()).stream().map(r -> {
                            Operateur o = operateurs.parId(r.operateur()).orElseThrow();
                            return new UtilisateurLigne.RattachementLigne(r.id(), o.id(), o.code().valeur(),
                                    o.identite().raisonSociale(), o.type(), r.role(), r.statut(), o.estActif());
                        }).toList());
            }
        };
    }

    // Données

    /** Un opérateur producteur, actif. */
    long operateur(String code, String raisonSociale) {
        return operateurs.inserer(Operateur.creer(TypeOperateur.PRODUCTEUR, CodeOperateur.de(code), identite(raisonSociale)))
                .id();
    }

    /** Un membre : utilisateur (créé au besoin), fiche et rattachement actif, comme après une invitation acceptée. */
    Acteur membre(long operateur, Role role, String prenom, String nom) {
        String email = (prenom + "." + nom + "@exemple.fr").toLowerCase();
        Utilisateur utilisateur = utilisateurs.parEmail(Email.de(email, "test")).orElseGet(() -> utilisateurs.inserer(
                Utilisateur.creer(IdentitePersonne.utilisateur(null, nom, prenom), Email.de(email, "test"), operateur)));
        FicheProfessionnelle fiche = fiches().inserer(FicheProfessionnelle.creer(operateur, TypeFiche.RATTACHEMENT,
                CoordonneesProfessionnelles.de("Gérante", "05 62 00 00 00"), "test", horloge.instant()));
        rattachements.inserer(Rattachement.creer(operateur, utilisateur.id(), role, fiche.id(), horloge.instant()));
        return new Acteur(utilisateur.id());
    }

    /** Un contact et sa fiche. */
    Contact contact(long operateur, String prenom, String nom, String email) {
        FicheProfessionnelle fiche = fiches().inserer(FicheProfessionnelle.creer(operateur, TypeFiche.CONTACT,
                CoordonneesProfessionnelles.de("Maître de chai", null), "test", horloge.instant()));
        return contacts().inserer(Contact.creer(operateur, new IdentitePersonne(null, nom, prenom),
                email == null ? null : Email.de(email, "test"), fiche.id()));
    }

    Rattachement rattachementDe(Acteur acteur, long operateur) {
        return rattachements.de(operateur, acteur.utilisateur()).orElseThrow();
    }

    static IdentiteOperateur identite(String raisonSociale) {
        return new IdentiteOperateur(null, null, null, null, null, null, null, null, raisonSociale, null,
                new Adresse("1 route d'Auch", null, null, "32100", "Condom", FRANCE), null, null, null, null, null, null,
                null, null, false, null);
    }

    // Dépôts

    /** Horloge réglable : les expirations se testent en avançant le temps. */
    static final class Horloge extends Clock {

        private Instant maintenant;

        Horloge(Instant depart) {
            this.maintenant = depart;
        }

        void avancer(Duration duree) {
            maintenant = maintenant.plus(duree);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return maintenant;
        }
    }

    /** Un dépôt qui attribue les identifiants ; {@code copie} reconstruit l'objet avec un id et une version. */
    abstract static class Depot<T> {

        final Map<Long, T> lignes = new LinkedHashMap<>();
        private long prochain = 1;
        private final Function<T, Long> id;

        Depot(Function<T, Long> id) {
            this.id = id;
        }

        abstract T copie(T objet, long id, boolean nouvelleVersion);

        public Optional<T> parId(long cle) {
            return Optional.ofNullable(lignes.get(cle));
        }

        public T inserer(T objet) {
            T enregistre = copie(objet, prochain++, false);
            lignes.put(id.apply(enregistre), enregistre);
            return enregistre;
        }

        public void mettreAJour(T objet) {
            lignes.put(id.apply(objet), copie(objet, id.apply(objet), true));
        }

        public void supprimer(long cle) {
            lignes.remove(cle);
        }

        List<T> tous() {
            return new ArrayList<>(lignes.values());
        }
    }

    static final class Contacts extends Depot<Contact> implements ContactRepository {

        Contacts() {
            super(Contact::id);
        }

        @Override
        Contact copie(Contact c, long id, boolean nouvelleVersion) {
            return Contact.reconstituer(id, c.operateur(), c.identite(), c.email(), c.fiche(),
                    nouvelleVersion ? c.version() + 1 : c.version());
        }

        @Override
        public Optional<Contact> parFiche(long fiche) {
            return lignes.values().stream().filter(c -> c.fiche() == fiche).findFirst();
        }

        @Override
        public Optional<Contact> parEmail(long operateur, Email email) {
            return lignes.values().stream().filter(c -> c.operateur() == operateur && c.aEmail(email)).findFirst();
        }

        @Override
        public boolean existeEmail(long operateur, Email email, Long sauf) {
            return lignes.values().stream().anyMatch(c -> c.operateur() == operateur && c.aEmail(email)
                    && !c.id().equals(sauf));
        }
    }

    static final class Fiches extends Depot<FicheProfessionnelle> implements FicheRepository {

        Fiches() {
            super(FicheProfessionnelle::id);
        }

        @Override
        FicheProfessionnelle copie(FicheProfessionnelle f, long id, boolean nouvelleVersion) {
            return FicheProfessionnelle.reconstituer(id, f.operateur(), f.type(), f.coordonnees(), f.modifieePar(),
                    f.modifieeLe(), nouvelleVersion ? f.version() + 1 : f.version());
        }
    }

    static final class Utilisateurs extends Depot<Utilisateur> implements UtilisateurRepository {

        Utilisateurs() {
            super(Utilisateur::id);
        }

        @Override
        Utilisateur copie(Utilisateur u, long id, boolean nouvelleVersion) {
            return Utilisateur.reconstituer(id, u.identite(), u.email(), u.statut(), u.operateurActif().orElse(null),
                    u.changementEmail().orElse(null), nouvelleVersion ? u.version() + 1 : u.version());
        }

        @Override
        public Optional<Utilisateur> parEmail(Email email) {
            return lignes.values().stream().filter(u -> u.email().cle().equals(email.cle())).findFirst();
        }

        @Override
        public Optional<Utilisateur> parEmpreinteChangementEmail(String empreinte) {
            return lignes.values().stream()
                    .filter(u -> u.changementEmail().filter(c -> c.empreinte().equals(empreinte)).isPresent())
                    .findFirst();
        }
    }

    static final class Rattachements extends Depot<Rattachement> implements RattachementRepository {

        Rattachements() {
            super(Rattachement::id);
        }

        @Override
        Rattachement copie(Rattachement r, long id, boolean nouvelleVersion) {
            return Rattachement.reconstituer(id, r.operateur(), r.utilisateur(), r.role(), r.statut(), r.fiche(),
                    r.creeLe());
        }

        @Override
        public Optional<Rattachement> parFiche(long fiche) {
            return lignes.values().stream().filter(r -> r.fiche() == fiche).findFirst();
        }

        @Override
        public Optional<Rattachement> de(long operateur, long utilisateur) {
            return lignes.values().stream().filter(r -> r.operateur() == operateur && r.utilisateur() == utilisateur)
                    .findFirst();
        }

        @Override
        public List<Rattachement> deLUtilisateur(long utilisateur) {
            return lignes.values().stream().filter(r -> r.utilisateur() == utilisateur).toList();
        }

        @Override
        public List<Rattachement> deLOperateur(long operateur) {
            return lignes.values().stream().filter(r -> r.operateur() == operateur).toList();
        }
    }

    static final class Invitations extends Depot<Invitation> implements InvitationRepository {

        Invitations() {
            super(Invitation::id);
        }

        @Override
        Invitation copie(Invitation i, long id, boolean nouvelleVersion) {
            return Invitation.reconstituer(id, i.operateur(), i.email(), i.role(), i.statut(), i.expireLe(),
                    i.empreinte(), i.fiche().orElse(null), i.personne().orElse(null), i.fonction(),
                    i.telephoneProfessionnel(), i.envoyeePar(), i.envoyeeLe(), i.accepteeLe().orElse(null),
                    i.utilisateur().orElse(null), i.compteVise().orElse(null));
        }

        @Override
        public Optional<Invitation> parEmpreinte(String empreinte) {
            return lignes.values().stream().filter(i -> i.empreinte().equals(empreinte)).findFirst();
        }

        @Override
        public Optional<Invitation> enAttentePourEmail(long operateur, Email email) {
            return lignes.values().stream().filter(i -> i.operateur() == operateur && i.estEnAttente()
                    && i.email().cle().equals(email.cle())).findFirst();
        }

        @Override
        public Optional<Invitation> enAttentePourFiche(long fiche) {
            return lignes.values().stream().filter(i -> i.estEnAttente() && i.fiche().filter(f -> f == fiche).isPresent())
                    .findFirst();
        }

        @Override
        public List<Invitation> enAttente(long operateur) {
            return lignes.values().stream().filter(i -> i.operateur() == operateur && i.estEnAttente()).toList();
        }

        @Override
        public List<Invitation> echues(Instant maintenant) {
            return lignes.values().stream()
                    .filter(i -> i.statut() == StatutInvitation.EN_ATTENTE && !maintenant.isBefore(i.expireLe()))
                    .toList();
        }
    }

    static final class Journal implements JournalActions {

        final List<LigneJournal> lignes = new ArrayList<>();

        @Override
        public void ajouter(LigneJournal ligne) {
            lignes.add(ligne);
        }

        @Override
        public List<LigneJournal> deLOperateur(long operateur) {
            return lignes.stream().filter(l -> l.operateur() != null && l.operateur() == operateur)
                    .sorted(Comparator.comparing(LigneJournal::date).reversed()).toList();
        }

        @Override
        public List<LigneJournal> deLaCible(TypeCible cible, long id) {
            return lignes.stream().filter(l -> l.cible() == cible && l.idCible() == id)
                    .sorted(Comparator.comparing(LigneJournal::date).reversed()).toList();
        }

        LigneJournal derniere() {
            return lignes.getLast();
        }
    }

    static final class Courriels implements EnvoiCourriels {

        final List<Courriel> envoyes = new ArrayList<>();

        @Override
        public void envoyer(Courriel courriel) {
            envoyes.add(courriel);
        }

        /** Le jeton du dernier lien envoyé à cette adresse. */
        String jeton(String destinataire) {
            Courriel courriel = envoyes.reversed().stream().filter(c -> c.destinataire().equals(destinataire))
                    .findFirst().orElseThrow();
            String texte = courriel.texte();
            int debut = texte.indexOf("jeton=") + "jeton=".length();
            int fin = texte.indexOf('\n', debut);
            return java.net.URLDecoder.decode(texte.substring(debut, fin), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    static final class PresentationsEnMemoire implements PresentationRepository {

        private final Map<Long, fr.bnia.operateurs.domain.PresentationOperateur> presentations = new LinkedHashMap<>();

        @Override
        public Optional<fr.bnia.operateurs.domain.PresentationOperateur> parId(long operateur) {
            return Optional.ofNullable(presentations.get(operateur));
        }

        @Override
        public void enregistrer(fr.bnia.operateurs.domain.PresentationOperateur presentation) {
            presentations.put(presentation.id(), presentation);
        }
    }
}
