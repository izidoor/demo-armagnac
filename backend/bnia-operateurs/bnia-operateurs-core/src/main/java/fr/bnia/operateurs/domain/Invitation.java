package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.partage.Role;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Invitation (vision §2.2, UC-12, 13, 20 ; spec 02 §3.2.5) : le lien d'activation qui prouve l'e-mail et
 * donne l'accès à un opérateur. Racine de son agrégat (A-OPE-6). Elle part d'un contact de l'opérateur
 * (sa fiche) ou d'une personne nouvelle, dont elle garde l'identité ; elle ne crée rien avant d'être
 * acceptée (RG-OPE-40).
 */
public final class Invitation {

    private final Long id;
    private final long operateur;
    private final Email email;
    private final Role role;
    private StatutInvitation statut;
    private Instant expireLe;
    private String empreinte;
    private final Long fiche;
    private final IdentitePersonne personne;
    private final String fonction;
    private final String telephoneProfessionnel;
    private final String envoyeePar;
    private final Instant envoyeeLe;
    private Instant accepteeLe;
    private Long utilisateur;
    private final Long compteVise;

    private Invitation(Long id, long operateur, Email email, Role role, StatutInvitation statut, Instant expireLe,
            String empreinte, Long fiche, IdentitePersonne personne, String fonction, String telephoneProfessionnel,
            String envoyeePar, Instant envoyeeLe, Instant accepteeLe, Long utilisateur, Long compteVise) {
        this.id = id;
        this.operateur = operateur;
        this.email = email;
        this.role = role;
        this.statut = statut;
        this.expireLe = expireLe;
        this.empreinte = empreinte;
        this.fiche = fiche;
        this.personne = personne;
        this.fonction = Textes.facultatif(fonction);
        this.telephoneProfessionnel = Textes.facultatif(telephoneProfessionnel);
        this.envoyeePar = envoyeePar;
        this.envoyeeLe = envoyeeLe;
        this.accepteeLe = accepteeLe;
        this.utilisateur = utilisateur;
        this.compteVise = compteVise;
    }

    /** Une invitation et son jeton en clair, à mettre dans le lien envoyé (RG-OPE-40). */
    public record Emise(Invitation invitation, String jeton) {
    }

    /**
     * RG-OPE-40 : depuis un contact de l'opérateur (sa fiche) ou une personne nouvelle ({@code personne}
     * non vide) ; e-mail et rôle obligatoires ; en attente, valable le temps donné.
     *
     * @param fiche    fiche du contact invité, ou {@code null} pour une personne nouvelle
     * @param personne   identité d'une personne nouvelle (nom et prénom obligatoires), ou {@code null}
     * @param compteVise l'utilisateur qui a cet e-mail à l'envoi, ou {@code null} (RG-OPE-42)
     */
    public static Emise emettre(long operateur, Email email, Role role, Long fiche, IdentitePersonne personne,
            String fonction, String telephoneProfessionnel, String auteur, Instant maintenant, Duration validite,
            Long compteVise) {
        if (email == null) {
            throw new RegleEnfreinte("RG-OPE-40", "L'e-mail de la personne invitée est obligatoire.");
        }
        if (role == null) {
            throw new RegleEnfreinte("RG-OPE-40", "Le rôle à accorder est obligatoire : observateur, agent ou admin.");
        }
        if ((fiche == null) == (personne == null)) {
            throw new IllegalArgumentException("Une invitation part d'un contact ou d'une personne nouvelle.");
        }
        if (personne != null && personne.prenom() == null) {
            throw new RegleEnfreinte("RG-OPE-40", "Le prénom de la personne invitée est obligatoire.");
        }
        Jeton jeton = Jeton.nouveau();
        Invitation invitation = new Invitation(null, operateur, email, role, StatutInvitation.EN_ATTENTE,
                maintenant.plus(validite), jeton.empreinte(), fiche, personne, fonction, telephoneProfessionnel, auteur,
                maintenant, null, null, compteVise);
        return new Emise(invitation, jeton.clair());
    }

    public static Invitation reconstituer(Long id, long operateur, Email email, Role role, StatutInvitation statut,
            Instant expireLe, String empreinte, Long fiche, IdentitePersonne personne, String fonction,
            String telephoneProfessionnel, String envoyeePar, Instant envoyeeLe, Instant accepteeLe, Long utilisateur,
            Long compteVise) {
        return new Invitation(id, operateur, email, role, statut, expireLe, empreinte, fiche, personne, fonction,
                telephoneProfessionnel, envoyeePar, envoyeeLe, accepteeLe, utilisateur, compteVise);
    }

    /** RG-OPE-41 : nouveau jeton (l'ancien devient invalide), nouvelle expiration ; renvoie le jeton en clair. */
    public String relancer(Instant maintenant, Duration validite) {
        exigerEnAttente("relancée");
        Jeton jeton = Jeton.nouveau();
        this.empreinte = jeton.empreinte();
        this.expireLe = maintenant.plus(validite);
        return jeton.clair();
    }

    /** RG-OPE-41 : le contact dont elle partait reste inchangé. */
    public void annuler() {
        exigerEnAttente("annulée");
        this.statut = StatutInvitation.ANNULEE;
    }

    /** RG-OPE-41 : vrai si l'invitation vient de passer à {@code EXPIREE}. */
    public boolean expirerSiEchue(Instant maintenant) {
        if (statut == StatutInvitation.EN_ATTENTE && !maintenant.isBefore(expireLe)) {
            this.statut = StatutInvitation.EXPIREE;
            return true;
        }
        return false;
    }

    /** RG-OPE-42 : le jeton doit correspondre à une invitation en attente, non expirée. */
    public void verifierAcceptable(Instant maintenant) {
        switch (statut) {
            case ACCEPTEE -> throw new RegleEnfreinte("RG-OPE-42", "Cette invitation a déjà été acceptée : connectez-vous.");
            case ANNULEE -> throw new RegleEnfreinte("RG-OPE-42", "Cette invitation a été annulée.");
            case EXPIREE -> throw expiree();
            case EN_ATTENTE -> {
                if (!maintenant.isBefore(expireLe)) {
                    throw expiree();
                }
            }
        }
    }

    /**
     * RG-OPE-42 : une invitation envoyée à l'e-mail d'un compte n'est acceptée que par ce compte, tant qu'il
     * a cet e-mail ; s'il en a changé depuis (RG-OPE-48), elle ne peut plus l'être.
     *
     * @param titulaire le compte qui a aujourd'hui l'e-mail de l'invitation, ou {@code null}
     */
    public void verifierCompteVise(Long titulaire) {
        if (compteVise != null && !compteVise.equals(titulaire)) {
            throw new RegleEnfreinte("RG-OPE-42", "Cette invitation était adressée à un compte qui a changé d'e-mail"
                    + " depuis : demandez à l'opérateur de vous la renvoyer.");
        }
    }

    /** RG-OPE-42 : acceptée par cet utilisateur. */
    public void accepter(long parUtilisateur, Instant maintenant) {
        verifierAcceptable(maintenant);
        this.statut = StatutInvitation.ACCEPTEE;
        this.accepteeLe = maintenant;
        this.utilisateur = parUtilisateur;
    }

    /** RG-AUT-15 : l'utilisateur qui l'a acceptée il y a moins que le délai donné. */
    public Optional<Long> accepteeRecemmentPar(Instant maintenant, Duration delai) {
        if (statut != StatutInvitation.ACCEPTEE || accepteeLe == null || utilisateur == null
                || accepteeLe.plus(delai).isBefore(maintenant)) {
            return Optional.empty();
        }
        return Optional.of(utilisateur);
    }

    public boolean estEnAttente() {
        return statut == StatutInvitation.EN_ATTENTE;
    }

    private void exigerEnAttente(String participe) {
        if (statut != StatutInvitation.EN_ATTENTE) {
            throw new RegleEnfreinte("RG-OPE-41", "Seule une invitation en attente peut être " + participe + ".");
        }
    }

    private static RegleEnfreinte expiree() {
        return new RegleEnfreinte("RG-OPE-42", "Cette invitation a expiré : demandez à l'opérateur de vous la renvoyer.");
    }

    public Long id() {
        return id;
    }

    public long operateur() {
        return operateur;
    }

    public Email email() {
        return email;
    }

    public Role role() {
        return role;
    }

    public StatutInvitation statut() {
        return statut;
    }

    public Instant expireLe() {
        return expireLe;
    }

    public String empreinte() {
        return empreinte;
    }

    /** Fiche du contact dont elle part ; vide pour une personne nouvelle. */
    public Optional<Long> fiche() {
        return Optional.ofNullable(fiche);
    }

    /** Identité d'une personne nouvelle ; vide si l'invitation part d'un contact. */
    public Optional<IdentitePersonne> personne() {
        return Optional.ofNullable(personne);
    }

    public String fonction() {
        return fonction;
    }

    public String telephoneProfessionnel() {
        return telephoneProfessionnel;
    }

    public String envoyeePar() {
        return envoyeePar;
    }

    public Instant envoyeeLe() {
        return envoyeeLe;
    }

    public Optional<Instant> accepteeLe() {
        return Optional.ofNullable(accepteeLe);
    }

    public Optional<Long> utilisateur() {
        return Optional.ofNullable(utilisateur);
    }

    public Optional<Long> compteVise() {
        return Optional.ofNullable(compteVise);
    }
}
