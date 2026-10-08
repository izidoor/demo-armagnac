package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Utilisateur (vision §2.2, spec 02 §3.2.2) : une personne avec un compte, propriétaire de son identité.
 * N'existe qu'une fois son e-mail prouvé, à l'acceptation d'une invitation (vision principe 3). Racine de
 * son agrégat, avec sa demande de changement d'e-mail (A-OPE-6). Le mot de passe est à
 * {@code authentification}.
 */
public final class Utilisateur {

    private final Long id;
    private IdentitePersonne identite;
    private Email email;
    private StatutUtilisateur statut;
    private Long operateurActif;
    private ChangementEmail changementEmail;
    private final long version;

    private Utilisateur(Long id, IdentitePersonne identite, Email email, StatutUtilisateur statut, Long operateurActif,
            ChangementEmail changementEmail, long version) {
        this.id = id;
        this.identite = identite;
        this.email = email;
        this.statut = statut;
        this.operateurActif = operateurActif;
        this.changementEmail = changementEmail;
        this.version = version;
    }

    /**
     * RG-OPE-27, 42 : à l'acceptation d'une invitation ; actif, l'opérateur de l'invitation pour
     * opérateur actif.
     */
    public static Utilisateur creer(IdentitePersonne identite, Email email, long operateurActif) {
        return new Utilisateur(null, identite, email, StatutUtilisateur.ACTIF, operateurActif, null, 0);
    }

    public static Utilisateur reconstituer(Long id, IdentitePersonne identite, Email email, StatutUtilisateur statut,
            Long operateurActif, ChangementEmail changementEmail, long version) {
        return new Utilisateur(id, identite, email, statut, operateurActif, changementEmail, version);
    }

    /** RG-OPE-47, 51 : par l'utilisateur seul ; renvoie les champs changés (RG-OPE-34). */
    public List<Changement> modifierProfil(IdentitePersonne nouvelle, Long versionLue) {
        Versions.verifier(versionLue, version, "Votre profil");
        List<Changement> changements = Changement.identite(identite, nouvelle);
        this.identite = nouvelle;
        return changements;
    }

    /**
     * RG-OPE-48 : une seule demande en cours (la nouvelle remplace la précédente), valable le temps donné.
     *
     * @return le jeton en clair, à envoyer au nouvel e-mail
     */
    public String demanderChangementEmail(Email nouvel, Instant maintenant, Duration validite) {
        if (nouvel.cle().equals(email.cle())) {
            throw new RegleEnfreinte("RG-OPE-48", "C'est déjà votre e-mail.");
        }
        Jeton jeton = Jeton.nouveau();
        this.changementEmail = new ChangementEmail(nouvel, jeton.empreinte(), maintenant.plus(validite));
        return jeton.clair();
    }

    /** RG-OPE-48 : la demande en cours est confirmée par son lien ; l'e-mail change, la demande est close. */
    public List<Changement> confirmerChangementEmail(Instant maintenant) {
        if (changementEmail == null || changementEmail.expiree(maintenant)) {
            throw new RegleEnfreinte("RG-OPE-48", "Ce lien de confirmation a expiré : refaites la demande depuis"
                    + " « Mon compte ».");
        }
        List<Changement> changements = new ArrayList<>();
        Changement.comparer(changements, ChampJournal.EMAIL, email.valeur(), changementEmail.nouvelEmail().valeur());
        this.email = changementEmail.nouvelEmail();
        this.changementEmail = null;
        return changements;
    }

    /** RG-OPE-50 : par un ADMIN BNIA, motif obligatoire ; rien d'autre ne change. */
    public void bloquer(String motif) {
        if (Textes.facultatif(motif) == null) {
            throw new RegleEnfreinte("RG-OPE-50", "Le motif du blocage est obligatoire.");
        }
        if (statut == StatutUtilisateur.BLOQUE) {
            throw new RegleEnfreinte("RG-OPE-50", identite.nomComplet() + " est déjà bloqué.");
        }
        this.statut = StatutUtilisateur.BLOQUE;
    }

    /** RG-OPE-50 : rétablit la situation précédente. */
    public void debloquer() {
        if (statut != StatutUtilisateur.BLOQUE) {
            throw new RegleEnfreinte("RG-OPE-50", identite.nomComplet() + " n'est pas bloqué.");
        }
        this.statut = StatutUtilisateur.ACTIF;
    }

    /** RG-OPE-49 : l'opérateur où l'application s'ouvre directement. */
    public void choisirOperateurActif(Long operateur) {
        this.operateurActif = operateur;
    }

    /** RG-OPE-42 : à la première invitation acceptée, l'opérateur de l'invitation. */
    public void proposerOperateurActif(long operateur) {
        if (operateurActif == null) {
            operateurActif = operateur;
        }
    }

    public boolean estBloque() {
        return statut == StatutUtilisateur.BLOQUE;
    }

    public Long id() {
        return id;
    }

    public IdentitePersonne identite() {
        return identite;
    }

    public Email email() {
        return email;
    }

    public StatutUtilisateur statut() {
        return statut;
    }

    public Optional<Long> operateurActif() {
        return Optional.ofNullable(operateurActif);
    }

    public Optional<ChangementEmail> changementEmail() {
        return Optional.ofNullable(changementEmail);
    }

    public long version() {
        return version;
    }
}
