package fr.bnia.operateurs.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.ActionJournal;
import fr.bnia.operateurs.domain.Adresse;
import fr.bnia.operateurs.domain.Alambic;
import fr.bnia.operateurs.domain.CodeOperateur;
import fr.bnia.operateurs.domain.CoordonneesGps;
import fr.bnia.operateurs.domain.Cvi;
import fr.bnia.operateurs.domain.Declaratif;
import fr.bnia.operateurs.domain.DestinataireFactures;
import fr.bnia.operateurs.domain.DiffusionMillesimes;
import fr.bnia.operateurs.domain.Email;
import fr.bnia.operateurs.domain.IdentiteOperateur;
import fr.bnia.operateurs.domain.Invitation;
import fr.bnia.operateurs.domain.NumeroAccise;
import fr.bnia.operateurs.domain.NumeroTva;
import fr.bnia.operateurs.domain.Operateur;
import fr.bnia.operateurs.domain.Production;
import fr.bnia.operateurs.domain.ProfilOperateur;
import fr.bnia.operateurs.domain.Rattachement;
import fr.bnia.operateurs.domain.SerieCode;
import fr.bnia.operateurs.domain.Siret;
import fr.bnia.operateurs.domain.SiteWeb;
import fr.bnia.operateurs.domain.Textes;
import fr.bnia.operateurs.domain.TypeCible;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.EtatConnexion;
import fr.bnia.operateurs.domain.partage.Module;
import fr.bnia.operateurs.domain.partage.ResultatRecherche;
import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import fr.bnia.referentiels.domain.partage.PaysVue;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Opérateurs (spec 02 §5.1) : listing, création avec code proposé et premier ADMIN, identité, profil,
 * production, diffusion, désignations, nouveau code, désactivation, suppression. Module Contacts
 * (RG-OPE-31) ; les champs réservés et la fin d'un opérateur sont au BNIA (RG-OPE-25, 26, 32, 36).
 */
@UseCase
public class GererOperateurs {

    /** Code ISO de la France : seul pays dont le code postal donne un département (RG-OPE-03). */
    private static final String FRANCE = "FR";

    private final OperateurRepository repository;
    private final RattachementRepository rattachements;
    private final InvitationRepository invitations;
    private final ListingOperateurs listing;
    private final NumerotationLecture numerotation;
    private final ReferentielsLecture referentiels;
    private final OperateursLecture lecture;
    private final Habilitations habilitations;
    private final GererInvitations gererInvitations;
    private final References references;
    private final Journaliste journaliste;
    private final Personnes personnes;

    public GererOperateurs(OperateurRepository repository, ContactRepository contacts, FicheRepository fiches,
            RattachementRepository rattachements, UtilisateurRepository utilisateurs, InvitationRepository invitations,
            ListingOperateurs listing, NumerotationLecture numerotation, ReferentielsLecture referentiels,
            OperateursLecture lecture, JournalActions journal, PublicationEvenements publication,
            Habilitations habilitations, GererInvitations gererInvitations, Clock horloge) {
        this.repository = repository;
        this.rattachements = rattachements;
        this.invitations = invitations;
        this.listing = listing;
        this.numerotation = numerotation;
        this.referentiels = referentiels;
        this.lecture = lecture;
        this.habilitations = habilitations;
        this.gererInvitations = gererInvitations;
        this.references = new References(referentiels);
        this.journaliste = new Journaliste(journal, utilisateurs, referentiels, horloge);
        this.personnes = new Personnes(repository, contacts, fiches, rattachements, utilisateurs, invitations,
                publication, journaliste);
    }

    /** RG-OPE-32, 39 : tous les opérateurs, sauf le BNIA, pour son personnel. */
    public List<OperateurLigne> lister(Acteur acteur, FiltreOperateurs filtre) {
        habilitations.exigerBnia(acteur, Module.CONTACTS, false);
        return listing.lister(filtre);
    }

    /** La fiche, avec ce qu'elle signale (RG-OPE-02, 05, 06, 09, 23) et les droits du lecteur (RG-OPE-31). */
    public FicheOperateur fiche(Acteur acteur, long id) {
        Droits droits = habilitations.exigerLecture(acteur, id, Module.CONTACTS);
        Operateur operateur = personnes.operateur(id);
        Optional<SerieCode> serieAttendue = operateur.serieAttendueSiCodeNonConforme(
                enFrance(operateur.identite().adresse().pays()));
        String groupe = operateur.identite().groupeNormalise();
        Optional<String> emailFacturation = emailFacturation(operateur);
        return new FicheOperateur(
                operateur,
                operateur.manquePourFacturer(emailFacturation.isPresent()),
                operateur.manquePourCertifier(),
                operateur.identite().tvaDiscordante(),
                serieAttendue.isEmpty(),
                serieAttendue.flatMap(this::suivant),
                groupe == null || operateur.estBnia() ? List.of() : listing.duGroupe(groupe, id),
                emailFacturation,
                droits);
    }

    /**
     * RG-OPE-03 : le code proposé pour un type et un code postal ; rien pour un négociant sans code
     * postal français.
     *
     * @param pays identifiant du pays ; {@code null} : la France
     */
    public Optional<CodeOperateur> proposerCode(Acteur acteur, TypeOperateur type, String codePostal, Long pays) {
        habilitations.exigerBnia(acteur, Module.CONTACTS, true);
        return codePropose(type, codePostal, pays);
    }

    /**
     * RG-OPE-01 à 08, 20, 21, 24, 32, 39, 40 : par le personnel du BNIA qui gère les contacts. Sans code
     * saisi, le code proposé est attribué (RG-OPE-03). Un ADMIN du BNIA peut inviter un premier ADMIN dans
     * le même formulaire.
     *
     * @param premierAdmin {@code null} : aucun ADMIN invité (RG-OPE-35 : un opérateur peut n'en avoir aucun)
     */
    public Operateur creer(Acteur acteur, SaisieOperateur saisie, SaisieInvitation premierAdmin) {
        habilitations.exigerBnia(acteur, Module.CONTACTS, true);
        if (premierAdmin != null) {
            habilitations.exigerAdminBnia(acteur);
        }
        Long pays = saisie.pays() != null ? saisie.pays() : france().map(PaysVue::id).orElse(null);
        IdentiteOperateur identite = identite(saisie, pays);
        verifierReferences(identite, null);
        CodeOperateur code = Textes.facultatif(saisie.code()) != null
                ? CodeOperateur.de(saisie.code())
                : codePropose(saisie.type(), identite.adresse().codePostal(), pays).orElseThrow(() -> new RegleEnfreinte(
                        "RG-OPE-02", "Le code opérateur est obligatoire : aucun code ne peut être proposé pour un"
                                + " négociant sans code postal français."));
        Operateur operateur = Operateur.creer(saisie.type(), code, identite);
        verifierUnicite(operateur, null);
        Operateur cree = repository.inserer(operateur);
        if (premierAdmin != null) {
            gererInvitations.emettre(acteur, cree.id(), new SaisieInvitation(null, premierAdmin.email(), Role.ADMIN,
                    premierAdmin.titre(), premierAdmin.nom(), premierAdmin.prenom(), premierAdmin.fonction(),
                    premierAdmin.telephoneProfessionnel()));
        }
        return cree;
    }

    /**
     * Onglet Général, identité complète (RG-OPE-01 à 09, 20, 21, 24, 36, 39, 51) : personnel du BNIA qui
     * gère les contacts. Changer le type ou le code postal ne change pas le code (RG-OPE-05).
     */
    public Operateur modifierIdentite(Acteur acteur, long id, SaisieOperateur saisie) {
        exigerGestionBnia(acteur, id);
        Operateur operateur = personnes.operateur(id);
        operateur.verifierVersion(saisie.version());
        IdentiteOperateur identite = identite(saisie, saisie.pays());
        verifierReferences(identite, operateur.identite());
        operateur.modifierIdentite(saisie.type(), CodeOperateur.de(saisie.code()), identite);
        verifierUnicite(operateur, id);
        repository.mettreAJour(operateur);
        return operateur;
    }

    /** RG-OPE-20, 21, 24, 31, 36, 51 : les champs qu'un opérateur modifie lui-même, par le module Contacts. */
    public Operateur modifierProfil(Acteur acteur, long id, SaisieProfilOperateur s) {
        habilitations.exigerGestion(acteur, id, Module.CONTACTS);
        Operateur operateur = personnes.operateur(id);
        operateur.verifierVersion(s.version());
        var profil = new ProfilOperateur(s.titre(), s.nomCommercial(),
                new Adresse(s.adresseLigne1(), s.adresseLigne2(), s.adresseLigne3(), s.codePostal(), s.ville(), s.pays()),
                s.telephone1(), s.telephone2(), s.portable(), Email.facultatif(s.emailGeneral(), "RG-OPE-24"),
                SiteWeb.facultatif(s.siteWeb()), SiteWeb.facultatif(s.siteCommercial()),
                CoordonneesGps.facultatives(s.latitude(), s.longitude()), s.accordDiffusionGeneral());
        IdentiteOperateur identite = operateur.identite().avecProfil(profil);
        verifierReferences(identite, operateur.identite());
        operateur.modifierIdentite(operateur.type(), operateur.code(), identite);
        repository.mettreAJour(operateur);
        return operateur;
    }

    /** Onglet Production (RG-OPE-01, 10 à 13, 21, 36, 51) : réservé au BNIA. */
    public Operateur modifierProduction(Acteur acteur, long id, SaisieProduction saisie) {
        exigerGestionBnia(acteur, id);
        Operateur operateur = personnes.operateur(id);
        operateur.verifierVersion(saisie.version());
        references.region(saisie.region(), operateur.production().region());
        List<Alambic> alambics = saisie.alambics() == null ? List.of() : saisie.alambics().stream()
                .map(a -> new Alambic(a.numeroAgrement(), a.type(), a.capacite()))
                .toList();
        operateur.modifierProduction(
                new Declaratif(saisie.periodicite(), saisie.statutDeclarationStock()),
                new Production(saisie.region(), saisie.appellations(), alambics));
        repository.mettreAJour(operateur);
        return operateur;
    }

    /**
     * Onglet Diffusion (RG-OPE-14, 15, 36, 51) : la diffusion des millésimes par le module Contacts ;
     * l'accès aux statistiques, par le BNIA seul.
     */
    public Operateur modifierDiffusion(Acteur acteur, long id, SaisieDiffusion saisie) {
        Droits droits = habilitations.exigerGestion(acteur, id, Module.CONTACTS);
        Operateur operateur = personnes.operateur(id);
        operateur.verifierVersion(saisie.version());
        if (saisie.accesStatistiques() != operateur.accesStatistiques() && !droits.personnelBnia()) {
            throw new AccesRefuse("RG-OPE-36", "L'accès aux statistiques du portail est fixé par le BNIA.");
        }
        operateur.modifierDiffusion(DiffusionMillesimes.saisie(saisie.niveau(), saisie.millesimes()));
        operateur.modifierAccesStatistiques(saisie.accesStatistiques());
        repository.mettreAJour(operateur);
        return operateur;
    }

    /**
     * RG-OPE-22, 23, 24, 31, 51 : deux fiches professionnelles de l'opérateur ; le destinataire des factures
     * a un e-mail, ou c'est une adresse saisie.
     */
    public Operateur designer(Acteur acteur, long id, SaisieDesignations saisie) {
        habilitations.exigerGestion(acteur, id, Module.CONTACTS);
        Operateur operateur = personnes.operateur(id);
        operateur.verifierVersion(saisie.version());
        Email adresse = Email.facultatif(saisie.destinataireEmail(), "RG-OPE-24");
        if (saisie.destinataireFiche() != null && adresse != null) {
            throw new RegleEnfreinte("RG-OPE-23", "Le destinataire des factures est soit une personne de l'annuaire,"
                    + " soit une adresse e-mail saisie, pas les deux.");
        }
        if (saisie.contactPrincipal() != null) {
            personnes.fiche(id, saisie.contactPrincipal());
        }
        DestinataireFactures destinataire = null;
        if (saisie.destinataireFiche() != null) {
            long fiche = personnes.fiche(id, saisie.destinataireFiche()).id();
            if (personnes.emailDeFiche(fiche).isEmpty()) {
                throw new RegleEnfreinte("RG-OPE-23", "Le destinataire des factures doit avoir un e-mail : ce n'est pas"
                        + " le cas de " + personnes.nomDeFiche(fiche) + ".");
            }
            destinataire = DestinataireFactures.fiche(fiche);
        } else if (adresse != null) {
            destinataire = DestinataireFactures.adresse(adresse);
        }
        operateur.designer(saisie.contactPrincipal(), destinataire);
        repository.mettreAJour(operateur);
        return operateur;
    }

    /** RG-OPE-37 : au moins trois caractères ; opérateurs actifs, sauf le BNIA, vingt au plus. */
    public List<ResultatRecherche> rechercher(Acteur acteur, String texte) {
        if (habilitations.etatConnexion(acteur.utilisateur()) != EtatConnexion.AUTORISE) {
            throw new AccesRefuse("RG-OPE-37", "La recherche d'opérateurs est réservée aux utilisateurs connectés.");
        }
        String recherche = Textes.facultatif(texte);
        if (recherche == null || recherche.length() < 3) {
            throw new RegleEnfreinte("RG-OPE-37", "Saisissez au moins trois caractères du code, de la raison sociale"
                    + " ou du nom commercial.");
        }
        return lecture.rechercher(recherche);
    }

    /** RG-OPE-05 : attribue le code proposé pour le type et le code postal actuels (RG-OPE-03). */
    public Operateur attribuerNouveauCode(Acteur acteur, long id) {
        exigerGestionBnia(acteur, id);
        Operateur operateur = personnes.operateur(id);
        CodeOperateur code = codePropose(operateur.type(), operateur.identite().adresse().codePostal(),
                operateur.identite().adresse().pays())
                .orElseThrow(() -> new RegleEnfreinte("RG-OPE-03",
                        "Aucun code ne peut être proposé : un négociant a besoin d'un code postal français."));
        operateur.recoder(code);
        verifierUnicite(operateur, id);
        repository.mettreAJour(operateur);
        return operateur;
    }

    /**
     * RG-OPE-26, 39 : par un ADMIN du BNIA. Reste consultable, n'est plus proposé ; ses rattachements sont
     * gelés, ses invitations en attente annulées.
     */
    public Operateur desactiver(Acteur acteur, long id) {
        habilitations.exigerAdminBnia(acteur);
        Operateur operateur = personnes.operateur(id);
        operateur.desactiver();
        repository.mettreAJour(operateur);
        for (Invitation invitation : invitations.enAttente(id)) {
            invitation.annuler();
            invitations.mettreAJour(invitation);
            journaliste.noter(acteur, id, ActionJournal.INVITATION_ANNULEE, TypeCible.INVITATION, invitation.id(),
                    invitation.personne().map(p -> p.nomComplet()).orElse(invitation.email().valeur()));
        }
        return operateur;
    }

    /** RG-OPE-26 : par un ADMIN du BNIA ; les rattachements reprennent tels quels. */
    public Operateur reactiver(Acteur acteur, long id) {
        habilitations.exigerAdminBnia(acteur);
        Operateur operateur = personnes.operateur(id);
        operateur.reactiver();
        repository.mettreAJour(operateur);
        return operateur;
    }

    /**
     * RG-OPE-25, 39, 44 : par un ADMIN du BNIA, seulement si rien ne le référence ; avec ses personnes. Un
     * utilisateur qui y perd son dernier rattachement est supprimé avec son compte de connexion.
     */
    public void supprimer(Acteur acteur, long id) {
        habilitations.exigerAdminBnia(acteur);
        Operateur operateur = personnes.operateur(id);
        operateur.verifierSuppressible();
        Set<Long> membres = rattachements.deLOperateur(id).stream().map(Rattachement::utilisateur)
                .collect(Collectors.toSet());
        repository.supprimer(id);
        membres.forEach(utilisateur -> personnes.supprimerUtilisateurSiSansRattachement(acteur, utilisateur));
    }

    /**
     * RG-OPE-06, 08 : avertissements non bloquants pendant la saisie. Une valeur mal formée, en cours de
     * frappe, est ignorée.
     *
     * @param sauf l'opérateur modifié, ou {@code null} à la création
     */
    public List<Doublon> doublons(Acteur acteur, String raisonSociale, String cvi, String siret, String accise,
            Long sauf) {
        habilitations.exigerBnia(acteur, Module.CONTACTS, false);
        String raison = Textes.facultatif(raisonSociale);
        return listing.doublons(raison == null ? null : Textes.normalise(raison), ouRien(() -> Cvi.facultatif(cvi)),
                ouRien(() -> Siret.facultatif(siret)), ouRien(() -> NumeroAccise.facultatif(accise)), sauf);
    }

    /** RG-OPE-09 : groupes existants proposés à la saisie. */
    public List<String> groupes(Acteur acteur, String recherche) {
        habilitations.exigerBnia(acteur, Module.CONTACTS, false);
        return listing.groupes(Textes.facultatif(recherche));
    }

    /** RG-OPE-32, 36 : identité complète, production, nouveau code : personnel du BNIA qui gère les contacts. */
    private void exigerGestionBnia(Acteur acteur, long id) {
        Droits droits = habilitations.exigerGestion(acteur, id, Module.CONTACTS);
        if (!droits.personnelBnia()) {
            throw new AccesRefuse("RG-OPE-36", "Ces informations de la fiche ne se modifient que par le BNIA.");
        }
    }

    /** RG-OPE-23 : l'adresse du destinataire : celle saisie, ou l'e-mail de la personne désignée. */
    private Optional<String> emailFacturation(Operateur operateur) {
        return operateur.destinataireFactures().flatMap(d -> d.adresse() != null
                ? Optional.of(d.adresse())
                : personnes.emailDeFiche(d.fiche())).map(Email::valeur);
    }

    private Optional<CodeOperateur> codePropose(TypeOperateur type, String codePostal, Long pays) {
        return SerieCode.pour(type, codePostal, pays == null || enFrance(pays)).flatMap(this::suivant);
    }

    private Optional<CodeOperateur> suivant(SerieCode serie) {
        return serie.suivant(numerotation.plusGrandNumero(serie));
    }

    private static IdentiteOperateur identite(SaisieOperateur s, Long pays) {
        return new IdentiteOperateur(
                s.groupe(),
                Cvi.facultatif(s.cvi()),
                Siret.facultatif(s.siret()),
                NumeroTva.facultatif(s.numeroTva()),
                NumeroAccise.facultatif(s.accise()),
                s.bureauDouanes(),
                s.identifiantOdg(),
                s.titre(),
                s.raisonSociale(),
                s.nomCommercial(),
                new Adresse(s.adresseLigne1(), s.adresseLigne2(), s.adresseLigne3(), s.codePostal(), s.ville(), pays),
                s.telephone1(),
                s.telephone2(),
                s.portable(),
                Email.facultatif(s.emailGeneral(), "RG-OPE-24"),
                SiteWeb.facultatif(s.siteWeb()),
                SiteWeb.facultatif(s.siteCommercial()),
                CoordonneesGps.facultatives(s.latitude(), s.longitude()),
                s.membreBnia(),
                s.accordDiffusionGeneral(),
                s.commentaireInterne());
    }

    /** RG-OPE-21. */
    private void verifierReferences(IdentiteOperateur nouvelle, IdentiteOperateur actuelle) {
        references.titre(nouvelle.titre(), actuelle == null ? null : actuelle.titre());
        references.pays(nouvelle.adresse().pays(), actuelle == null ? null : actuelle.adresse().pays());
        references.membreBnia(nouvelle.membreBnia(), actuelle == null ? null : actuelle.membreBnia());
    }

    /** RG-OPE-04, RG-OPE-07. */
    private void verifierUnicite(Operateur operateur, Long sauf) {
        if (repository.existeCode(operateur.code(), sauf)) {
            throw new ConflitUnicite("RG-OPE-04", "Le code « " + operateur.code() + " » est déjà attribué à un autre opérateur.");
        }
        String odg = operateur.identite().identifiantOdg();
        if (odg != null && repository.existeIdentifiantOdg(odg, sauf)) {
            throw new ConflitUnicite("RG-OPE-07", "L'identifiant tiers ODG « " + odg + " » est déjà porté par un autre opérateur.");
        }
    }

    private boolean enFrance(Long pays) {
        return pays != null && referentiels.pays(pays).map(p -> FRANCE.equals(p.codeIso())).orElse(false);
    }

    private Optional<PaysVue> france() {
        return referentiels.paysActifs().stream().filter(p -> FRANCE.equals(p.codeIso())).findFirst();
    }

    private static <T> T ouRien(Supplier<T> lecture) {
        try {
            return lecture.get();
        } catch (RegleEnfreinte malForme) {
            return null;
        }
    }
}
