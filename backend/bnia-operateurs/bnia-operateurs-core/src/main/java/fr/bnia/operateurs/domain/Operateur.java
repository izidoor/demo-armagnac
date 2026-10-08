package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.partage.AccesPortailAccorde;
import fr.bnia.operateurs.domain.partage.AccesPortailRetire;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Opérateur : structure suivie par le BNIA (§4.3 p.18-21, C.1 p.100-105). Racine de l'agrégat
 * {@code Operateur} (ADR-012) : identité, déclaratif, production, diffusion des millésimes, et ses
 * contacts, avec leurs droits au portail. Les règles qui lient opérateur et contacts (RG-OPE-22, 23, 28,
 * 30) sont garanties ici.
 *
 * <p>Les modifications de contacts produisent des faits : événements à publier (ADR-013) et entrées du
 * journal (RG-OPE-34), que le cas d'usage relève après l'enregistrement.
 */
public final class Operateur {

    private final Long id;
    private TypeOperateur type;
    private CodeOperateur code;
    private IdentiteOperateur identite;
    private Declaratif declaratif;
    private Production production;
    private DiffusionMillesimes diffusion;
    private boolean accesStatistiques;
    private boolean actif;
    private final List<Contact> contacts;
    private Long contactPrincipal;
    private DestinataireFactures destinataireFactures;
    private final List<Object> evenements = new ArrayList<>();
    private final List<EntreeJournal> journal = new ArrayList<>();

    private Operateur(Long id, TypeOperateur type, CodeOperateur code, IdentiteOperateur identite,
            Declaratif declaratif, Production production, DiffusionMillesimes diffusion, boolean accesStatistiques,
            boolean actif, Collection<Contact> contacts, Long contactPrincipal,
            DestinataireFactures destinataireFactures) {
        this.id = id;
        this.type = exigerType(type);
        this.code = code;
        this.identite = identite;
        declaratif.verifierPour(this.type);
        this.declaratif = declaratif;
        this.production = production;
        this.diffusion = diffusion;
        this.accesStatistiques = accesStatistiques;
        this.actif = actif;
        this.contacts = new ArrayList<>(contacts);
        this.contactPrincipal = contactPrincipal;
        this.destinataireFactures = destinataireFactures;
    }

    /**
     * RG-OPE-01, 02 : type, code et raison sociale obligatoires ; actif. Valeurs par défaut du type pour
     * le déclaratif (RG-OPE-10, 11), refus de diffusion (RG-OPE-14), pas d'accès aux statistiques
     * (RG-OPE-15).
     */
    public static Operateur creer(TypeOperateur type, CodeOperateur code, IdentiteOperateur identite) {
        TypeOperateur verifie = exigerType(type);
        return new Operateur(null, verifie, code, identite, Declaratif.parDefaut(verifie), Production.aucune(),
                DiffusionMillesimes.refus(), false, true, List.of(), null, null);
    }

    public static Operateur reconstituer(Long id, TypeOperateur type, CodeOperateur code, IdentiteOperateur identite,
            Declaratif declaratif, Production production, DiffusionMillesimes diffusion, boolean accesStatistiques,
            boolean actif, Collection<Contact> contacts, Long contactPrincipal,
            DestinataireFactures destinataireFactures) {
        return new Operateur(id, type, code, identite, declaratif, production, diffusion, accesStatistiques, actif,
                contacts, contactPrincipal, destinataireFactures);
    }

    /**
     * RG-OPE-05 : changer le type ou le code postal ne change pas le code ; seule une saisie explicite le
     * change (RG-OPE-04). Le déclaratif ne s'ajuste que si le nouveau type l'exige
     * ({@link Declaratif#ajustePour}).
     */
    public void modifierIdentite(TypeOperateur nouveauType, CodeOperateur nouveauCode, IdentiteOperateur nouvelle) {
        TypeOperateur verifie = exigerType(nouveauType);
        this.declaratif = declaratif.ajustePour(type, verifie);
        this.type = verifie;
        this.code = nouveauCode;
        this.identite = nouvelle;
    }

    /** Onglet Production : déclaratif contrôlé selon le type (RG-OPE-01, 10 à 13). */
    public void modifierProduction(Declaratif nouveauDeclaratif, Production nouvelle) {
        nouveauDeclaratif.verifierPour(type);
        this.declaratif = nouveauDeclaratif;
        this.production = nouvelle;
    }

    /** RG-OPE-14. */
    public void modifierDiffusion(DiffusionMillesimes nouvelle) {
        this.diffusion = nouvelle;
    }

    /** RG-OPE-15 : fixé par le BNIA. */
    public void modifierAccesStatistiques(boolean acces) {
        this.accesStatistiques = acces;
    }

    /** RG-OPE-05 : attribution d'un nouveau code, sur demande. */
    public void recoder(CodeOperateur nouveau) {
        this.code = nouveau;
    }

    /**
     * RG-OPE-26 : reste consultable ; ses contacts perdent l'accès au portail, qui ne revient pas à la
     * réactivation.
     */
    public void desactiver() {
        actif = false;
        contacts.forEach(this::retirerAcces);
    }

    public void reactiver() {
        actif = true;
    }

    /**
     * RG-OPE-02 : ce qui manque pour facturer ou certifier ; une fiche à laquelle il manque quelque chose
     * est « incomplète », mais s'enregistre.
     */
    public List<String> champsManquants() {
        List<String> manquants = new ArrayList<>();
        Adresse adresse = identite.adresse();
        if (adresse.ligne1() == null) {
            manquants.add("adresse");
        }
        if (adresse.codePostal() == null) {
            manquants.add("code postal");
        }
        if (adresse.ville() == null) {
            manquants.add("ville");
        }
        if (adresse.pays() == null) {
            manquants.add("pays");
        }
        if (production.region() == null) {
            manquants.add("région");
        }
        return manquants;
    }

    /**
     * RG-OPE-05 : la série à laquelle le code devrait appartenir, s'il n'en a plus la forme ; rien si le
     * code est conforme, ou si aucune série ne s'applique (négociant sans code postal français).
     *
     * @param enFrance vrai si le pays de l'adresse est la France
     */
    public Optional<SerieCode> serieAttendueSiCodeNonConforme(boolean enFrance) {
        return SerieCode.pour(type, identite.adresse().codePostal(), enFrance).filter(serie -> !serie.contient(code));
    }

    // Contacts (§4.3.4, C.1.9, §5.13)

    /** RG-OPE-27, 28 : actif, sans accès au portail. */
    public Contact ajouterContact(long idContact, IdentiteContact identite) {
        verifierEmailUnique(identite.email(), null);
        Contact contact = Contact.creer(idContact, identite);
        contacts.add(contact);
        journaliser(contact, ActionContact.CREATION, List.of());
        return contact;
    }

    /**
     * RG-OPE-28, 29, 32. Un e-mail qui est l'identifiant d'un compte ne change pas ici. Un e-mail changé
     * sur un contact qui a l'accès retire l'accès de l'ancienne adresse et l'accorde à la nouvelle.
     */
    public void modifierContact(long idContact, IdentiteContact nouvelle) {
        Contact contact = contact(idContact);
        IdentiteContact avant = contact.identite();
        boolean emailChange = !Objects.equals(texte(avant.email()), texte(nouvelle.email()));
        if (emailChange && contact.emailVerrouille()) {
            throw new RegleEnfreinte("RG-OPE-32", "L'e-mail de " + avant.nomComplet() + " est l'identifiant de son"
                    + " compte portail : seule cette personne peut le changer, dans « Mon compte ».");
        }
        verifierEmailUnique(nouvelle.email(), idContact);
        if (nouvelle.email() == null && contact.droits().acces()) {
            throw new RegleEnfreinte("RG-OPE-29", avant.nomComplet() + " a l'accès au portail : son e-mail est requis.");
        }
        if (nouvelle.email() == null && destinataireFactures != null
                && Objects.equals(destinataireFactures.contact(), idContact)) {
            throw new RegleEnfreinte("RG-OPE-23",
                    avant.nomComplet() + " reçoit les factures : son e-mail est requis.");
        }
        contact.identifier(nouvelle);
        if (emailChange && contact.droits().acces()) {
            evenements.add(new AccesPortailRetire(id, idContact, avant.email().valeur()));
            evenements.add(new AccesPortailAccorde(id, idContact, nouvelle.email().valeur()));
        }
        List<Changement> changements = Contact.changements(avant, nouvelle);
        if (!changements.isEmpty()) {
            journaliser(contact, ActionContact.MODIFICATION, changements);
        }
    }

    /** RG-OPE-29, 30, 31 : accès, rôle d'administrateur, modules. */
    public void modifierDroits(long idContact, DroitsPortail nouveaux, Canal canal) {
        Contact contact = contact(idContact);
        DroitsPortail avant = contact.droits();
        if (nouveaux.acces() && !contact.estActif()) {
            throw new RegleEnfreinte("RG-OPE-33",
                    contact.identite().nomComplet() + " est inactif : réactivez-le avant de lui accorder l'accès.");
        }
        if (nouveaux.acces() && contact.identite().email() == null) {
            throw new RegleEnfreinte("RG-OPE-29",
                    "L'accès au portail exige un e-mail : renseignez celui de " + contact.identite().nomComplet() + ".");
        }
        if (avant.administrateur() && !nouveaux.administrateur()) {
            verifierPasDernierAdministrateur(contact, canal);
        }
        contact.attribuer(nouveaux);
        List<Changement> changements = Contact.changements(avant, nouveaux);
        if (!avant.acces() && nouveaux.acces()) {
            evenements.add(new AccesPortailAccorde(id, idContact, contact.identite().email().valeur()));
            journaliser(contact, ActionContact.ACCES_ACCORDE, changements);
        } else if (avant.acces() && !nouveaux.acces()) {
            evenements.add(new AccesPortailRetire(id, idContact, contact.identite().email().valeur()));
            journaliser(contact, ActionContact.ACCES_RETIRE, changements);
        } else if (!changements.isEmpty()) {
            journaliser(contact, ActionContact.DROITS_MODIFIES, changements);
        }
    }

    /** RG-OPE-33 : retire l'accès (RG-OPE-29) et fait tomber les désignations (RG-OPE-22, 23). */
    public void desactiverContact(long idContact, Canal canal) {
        Contact contact = contact(idContact);
        verifierPasDernierAdministrateur(contact, canal);
        retirerAcces(contact);
        faireTomberDesignations(idContact);
        contact.desactiver();
        journaliser(contact, ActionContact.DESACTIVATION, List.of());
    }

    /** RG-OPE-33 : sans accès ; il se réaccorde. */
    public void reactiverContact(long idContact) {
        Contact contact = contact(idContact);
        contact.reactiver();
        journaliser(contact, ActionContact.REACTIVATION, List.of());
    }

    /** RG-OPE-33 : comme la désactivation ; le journal du contact est conservé (RG-OPE-34). */
    public void supprimerContact(long idContact, Canal canal) {
        Contact contact = contact(idContact);
        verifierPasDernierAdministrateur(contact, canal);
        retirerAcces(contact);
        faireTomberDesignations(idContact);
        contacts.remove(contact);
        journaliser(contact, ActionContact.SUPPRESSION, List.of());
    }

    /**
     * RG-OPE-22, 23, 24 : contact principal et destinataire des factures, contacts actifs de l'opérateur ;
     * le destinataire a un e-mail, ou c'est une adresse saisie.
     *
     * @param principal    identifiant du contact principal, ou {@code null}
     * @param destinataire {@code null} : aucun
     */
    public void designer(Long principal, DestinataireFactures destinataire) {
        if (principal != null) {
            Contact contact = contact(principal);
            if (!contact.estActif()) {
                throw new RegleEnfreinte("RG-OPE-22",
                        "Le contact principal doit être actif : " + contact.identite().nomComplet() + " ne l'est pas.");
            }
        }
        if (destinataire != null && destinataire.contact() != null) {
            Contact contact = contact(destinataire.contact());
            if (!contact.estActif() || contact.identite().email() == null) {
                throw new RegleEnfreinte("RG-OPE-23", "Le destinataire des factures doit être un contact actif avec un"
                        + " e-mail : ce n'est pas le cas de " + contact.identite().nomComplet() + ".");
            }
        }
        this.contactPrincipal = principal;
        this.destinataireFactures = destinataire;
    }

    /** RG-OPE-32 : l'e-mail est devenu l'identifiant d'un compte ; vrai si un contact est concerné. */
    public boolean verrouillerEmail(Email email) {
        List<Contact> concernes = contacts.stream().filter(c -> c.aEmail(email) && !c.emailVerrouille()).toList();
        concernes.forEach(Contact::verrouillerEmail);
        return !concernes.isEmpty();
    }

    /** RG-OPE-32 : la personne a changé l'identifiant de son compte ; ses contacts suivent (RG-OPE-28). */
    public void remplacerEmail(Email ancien, Email nouveau) {
        for (Contact contact : contacts.stream().filter(c -> c.aEmail(ancien)).toList()) {
            verifierEmailUnique(nouveau, contact.id());
            IdentiteContact avant = contact.identite();
            contact.identifier(avant.avecEmail(nouveau));
            journaliser(contact, ActionContact.MODIFICATION, Contact.changements(avant, contact.identite()));
        }
    }

    /** RG-OPE-23 : l'adresse à laquelle envoyer les factures, lue par {@code facturation}. */
    public Optional<Email> emailFacturation() {
        if (destinataireFactures == null) {
            return Optional.empty();
        }
        if (destinataireFactures.adresse() != null) {
            return Optional.of(destinataireFactures.adresse());
        }
        return contactParId(destinataireFactures.contact()).map(c -> c.identite().email());
    }

    public Contact contact(long idContact) {
        return contactParId(idContact).orElseThrow(() -> new ElementIntrouvable(
                "Contact " + idContact + " introuvable pour l'opérateur « " + code + " »."));
    }

    /** Les événements et entrées de journal produits depuis le chargement, puis oubliés. */
    public Faits releverFaits() {
        Faits faits = new Faits(List.copyOf(evenements), List.copyOf(journal));
        evenements.clear();
        journal.clear();
        return faits;
    }

    /** @param evenements types de {@code domain.partage} (ADR-013) */
    public record Faits(List<Object> evenements, List<EntreeJournal> journal) {
    }

    private Optional<Contact> contactParId(long idContact) {
        return contacts.stream().filter(c -> c.id() == idContact).findFirst();
    }

    /** RG-OPE-28 : unique dans l'opérateur, à la casse près. */
    private void verifierEmailUnique(Email email, Long sauf) {
        if (email == null) {
            return;
        }
        contacts.stream().filter(c -> !Objects.equals(c.id(), sauf) && c.aEmail(email)).findFirst().ifPresent(autre -> {
            throw new RegleEnfreinte("RG-OPE-28", "L'e-mail « " + email + " » est déjà celui de "
                    + autre.identite().nomComplet() + " dans cet opérateur.");
        });
    }

    /** RG-OPE-30 : au portail, l'opérateur garde au moins un administrateur actif. */
    private void verifierPasDernierAdministrateur(Contact contact, Canal canal) {
        if (canal == Canal.PORTAIL && contact.estAdministrateurActif()
                && contacts.stream().filter(Contact::estAdministrateurActif).count() == 1) {
            throw new RegleEnfreinte("RG-OPE-30", contact.identite().nomComplet() + " est le dernier administrateur"
                    + " de l'opérateur : adressez-vous au BNIA pour en désigner un autre.");
        }
    }

    private void retirerAcces(Contact contact) {
        if (contact.droits().acces()) {
            DroitsPortail avant = contact.droits();
            contact.attribuer(DroitsPortail.aucun());
            evenements.add(new AccesPortailRetire(id, contact.id(), contact.identite().email().valeur()));
            journaliser(contact, ActionContact.ACCES_RETIRE, Contact.changements(avant, contact.droits()));
        }
    }

    private void faireTomberDesignations(long idContact) {
        if (Objects.equals(contactPrincipal, idContact)) {
            contactPrincipal = null;
        }
        if (destinataireFactures != null && Objects.equals(destinataireFactures.contact(), idContact)) {
            destinataireFactures = null;
        }
    }

    private void journaliser(Contact contact, ActionContact action, List<Changement> changements) {
        journal.add(new EntreeJournal(contact.id(), contact.identite().nomComplet(), action, changements));
    }

    private static String texte(Object valeur) {
        return valeur == null ? null : valeur.toString();
    }

    private static TypeOperateur exigerType(TypeOperateur type) {
        if (type == null) {
            throw new RegleEnfreinte("RG-OPE-01", "Le type d'opérateur est obligatoire : producteur, négociant ou autre.");
        }
        return type;
    }

    public Long id() {
        return id;
    }

    public TypeOperateur type() {
        return type;
    }

    public CodeOperateur code() {
        return code;
    }

    public IdentiteOperateur identite() {
        return identite;
    }

    public Declaratif declaratif() {
        return declaratif;
    }

    public Production production() {
        return production;
    }

    public DiffusionMillesimes diffusion() {
        return diffusion;
    }

    public boolean accesStatistiques() {
        return accesStatistiques;
    }

    /** Triés par nom, puis prénom. */
    public List<Contact> contacts() {
        return contacts.stream()
                .sorted(Comparator.comparing((Contact c) -> c.identite().nom(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(c -> Objects.toString(c.identite().prenom(), "")))
                .toList();
    }

    public Optional<Long> contactPrincipal() {
        return Optional.ofNullable(contactPrincipal);
    }

    public Optional<DestinataireFactures> destinataireFactures() {
        return Optional.ofNullable(destinataireFactures);
    }

    public boolean estActif() {
        return actif;
    }
}
