package fr.bnia.operateurs.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.Canal;
import fr.bnia.operateurs.domain.Contact;
import fr.bnia.operateurs.domain.DestinataireFactures;
import fr.bnia.operateurs.domain.DroitsPortail;
import fr.bnia.operateurs.domain.Email;
import fr.bnia.operateurs.domain.IdentiteContact;
import fr.bnia.operateurs.domain.Operateur;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Contacts d'un opérateur et leurs droits au portail (§4.3.4 p.20-21, §5.13 p.78-79, C.1.9 p.104-105),
 * désignations du contact principal et du destinataire des factures, réactions aux événements de
 * {@code acces} (RG-OPE-32). Le canal ({@code BACK_OFFICE}, {@code PORTAIL}) décide de RG-OPE-30.
 */
@UseCase
public class GererContacts {

    private final OperateurRepository repository;
    private final IdentifiantsContacts identifiants;
    private final ReferentielsLecture referentiels;
    private final Enregistreur enregistreur;

    public GererContacts(OperateurRepository repository, IdentifiantsContacts identifiants, JournalContacts journal,
            PublicationEvenements publication, ReferentielsLecture referentiels, Clock horloge) {
        this.repository = repository;
        this.identifiants = identifiants;
        this.referentiels = referentiels;
        this.enregistreur = new Enregistreur(repository, journal, publication, referentiels, horloge);
    }

    public FicheContact consulter(long idOperateur, long idContact) {
        Operateur operateur = operateur(idOperateur);
        Contact contact = operateur.contact(idContact);
        return new FicheContact(contact, operateur.contactPrincipal().filter(id -> id == idContact).isPresent(),
                operateur.destinataireFactures().filter(d -> Objects.equals(d.contact(), idContact)).isPresent());
    }

    /** RG-OPE-34 : du plus récent au plus ancien, même après la suppression du contact. */
    public List<LigneJournal> journal(long operateur, long contact) {
        operateur(operateur);
        return enregistreur.journal(operateur, contact);
    }

    /** RG-OPE-21, 27, 28. */
    public Contact creer(long idOperateur, SaisieContact saisie, Canal canal) {
        Operateur operateur = operateur(idOperateur);
        IdentiteContact identite = identite(saisie);
        verifierReferences(identite, null);
        Contact contact = operateur.ajouterContact(identifiants.prochain(), identite);
        enregistreur.enregistrer(operateur, canal);
        return contact;
    }

    /** RG-OPE-21, 27, 28, 29, 32. */
    public Contact modifier(long idOperateur, long idContact, SaisieContact saisie, Canal canal) {
        Operateur operateur = operateur(idOperateur);
        IdentiteContact identite = identite(saisie);
        verifierReferences(identite, operateur.contact(idContact).identite());
        operateur.modifierContact(idContact, identite);
        enregistreur.enregistrer(operateur, canal);
        return operateur.contact(idContact);
    }

    /** RG-OPE-29, 30, 31. */
    public Contact modifierDroits(long idOperateur, long idContact, SaisieDroits saisie, Canal canal) {
        Operateur operateur = operateur(idOperateur);
        operateur.modifierDroits(idContact, new DroitsPortail(saisie.acces(), saisie.administrateur(), saisie.modules()),
                canal);
        enregistreur.enregistrer(operateur, canal);
        return operateur.contact(idContact);
    }

    /** RG-OPE-22, 23, 29, 30, 33. */
    public Contact desactiver(long idOperateur, long idContact, Canal canal) {
        Operateur operateur = operateur(idOperateur);
        operateur.desactiverContact(idContact, canal);
        enregistreur.enregistrer(operateur, canal);
        return operateur.contact(idContact);
    }

    /** RG-OPE-33. */
    public Contact reactiver(long idOperateur, long idContact, Canal canal) {
        Operateur operateur = operateur(idOperateur);
        operateur.reactiverContact(idContact);
        enregistreur.enregistrer(operateur, canal);
        return operateur.contact(idContact);
    }

    /** RG-OPE-22, 23, 29, 30, 33, 34. */
    public void supprimer(long idOperateur, long idContact, Canal canal) {
        Operateur operateur = operateur(idOperateur);
        operateur.supprimerContact(idContact, canal);
        enregistreur.enregistrer(operateur, canal);
    }

    /** RG-OPE-22, 23, 24. */
    public Operateur designer(long idOperateur, SaisieDesignations saisie, Canal canal) {
        Operateur operateur = operateur(idOperateur);
        Email adresse = Email.facultatif(saisie.destinataireEmail(), "RG-OPE-24");
        if (saisie.destinataireContact() != null && adresse != null) {
            throw new RegleEnfreinte("RG-OPE-23",
                    "Le destinataire des factures est soit un contact, soit une adresse e-mail saisie, pas les deux.");
        }
        DestinataireFactures destinataire = saisie.destinataireContact() != null
                ? DestinataireFactures.contact(saisie.destinataireContact())
                : adresse != null ? DestinataireFactures.adresse(adresse) : null;
        operateur.designer(saisie.contactPrincipal(), destinataire);
        enregistreur.enregistrer(operateur, canal);
        return operateur;
    }

    /** RG-OPE-32, à réception de « compte créé » : l'e-mail des contacts de ce compte se verrouille. */
    public void verrouillerEmail(String email) {
        Email adresse = Email.de(email, "RG-OPE-32");
        for (Operateur operateur : repository.avecEmailDeContact(adresse)) {
            if (operateur.verrouillerEmail(adresse)) {
                enregistreur.enregistrer(operateur, Canal.COMPTE);
            }
        }
    }

    /** RG-OPE-32, à réception de « identifiant modifié » : la nouvelle adresse remplace l'ancienne partout. */
    public void reporterIdentifiant(String ancien, String nouveau) {
        Email avant = Email.de(ancien, "RG-OPE-32");
        Email apres = Email.de(nouveau, "RG-OPE-32");
        for (Operateur operateur : repository.avecEmailDeContact(avant)) {
            operateur.remplacerEmail(avant, apres);
            enregistreur.enregistrer(operateur, Canal.COMPTE);
        }
    }

    private Operateur operateur(long id) {
        return repository.parId(id).orElseThrow(() -> new fr.bnia.commun.domain.ElementIntrouvable(
                "Opérateur " + id + " introuvable."));
    }

    private static IdentiteContact identite(SaisieContact s) {
        return new IdentiteContact(s.titre(), s.nom(), s.prenom(), s.fonction(), s.centresInteret(),
                s.telephoneProfessionnel(), s.telephoneSecondaire(), Email.facultatif(s.email(), "RG-OPE-28"),
                s.adresse(), s.codePostal(), s.ville(), s.pays(), s.observation());
    }

    /** RG-OPE-21 : une valeur choisie doit être active ; celle déjà portée reste acceptée. */
    private void verifierReferences(IdentiteContact nouvelle, IdentiteContact actuelle) {
        if (nouvelle.titre() != null && (actuelle == null || !Objects.equals(nouvelle.titre(), actuelle.titre()))) {
            verifierActive("titre", referentiels.titre(nouvelle.titre()).map(v -> new Valeur(v.libelle(), v.actif())),
                    nouvelle.titre());
        }
        if (nouvelle.pays() != null && (actuelle == null || !Objects.equals(nouvelle.pays(), actuelle.pays()))) {
            verifierActive("pays", referentiels.pays(nouvelle.pays()).map(v -> new Valeur(v.libelle(), v.actif())),
                    nouvelle.pays());
        }
        Set<Long> dejaPortes = actuelle == null ? Set.of() : actuelle.centresInteret();
        for (Long centre : nouvelle.centresInteret()) {
            if (!dejaPortes.contains(centre)) {
                verifierActive("centre d'intérêt",
                        referentiels.centreInteret(centre).map(v -> new Valeur(v.libelle(), v.actif())), centre);
            }
        }
    }

    private static void verifierActive(String nom, Optional<Valeur> valeur, long id) {
        Valeur trouvee = valeur.orElseThrow(() -> new RegleEnfreinte("RG-OPE-21", "Valeur inconnue (" + nom + ") : " + id + "."));
        if (!trouvee.actif()) {
            throw new RegleEnfreinte("RG-OPE-21",
                    "Valeur inactive (" + nom + ") : « " + trouvee.libelle() + " ». Choisissez une valeur active.");
        }
    }

    private record Valeur(String libelle, boolean actif) {
    }
}
