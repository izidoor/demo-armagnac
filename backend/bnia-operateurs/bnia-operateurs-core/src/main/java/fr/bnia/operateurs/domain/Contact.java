package fr.bnia.operateurs.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Contact d'un opérateur (§4.3.4 p.20-21, C.1.9 p.104-105, vision §2.2) : une personne sans compte, dont
 * l'identité appartient à l'opérateur. Racine de son agrégat (A-OPE-6). Ses informations
 * professionnelles sont dans sa fiche (RG-OPE-30). Il n'a pas d'état actif : on le supprime (RG-OPE-45).
 */
public final class Contact {

    private final Long id;
    private final long operateur;
    private IdentitePersonne identite;
    private Email email;
    private final long fiche;
    private final long version;

    private Contact(Long id, long operateur, IdentitePersonne identite, Email email, long fiche, long version) {
        this.id = id;
        this.operateur = operateur;
        this.identite = identite;
        this.email = email;
        this.fiche = fiche;
        this.version = version;
    }

    /** RG-OPE-27, 28, 30 : nom obligatoire, e-mail facultatif ; sa fiche est créée avant lui. */
    public static Contact creer(long operateur, IdentitePersonne identite, Email email, long fiche) {
        return new Contact(null, operateur, identite, email, fiche, 0);
    }

    public static Contact reconstituer(Long id, long operateur, IdentitePersonne identite, Email email, long fiche,
            long version) {
        return new Contact(id, operateur, identite, email, fiche, version);
    }

    /**
     * RG-OPE-27, 28, 51 : identité et e-mail, sur la version lue ; renvoie les champs changés (RG-OPE-34).
     */
    public List<Changement> modifier(IdentitePersonne nouvelle, Email nouvelEmail, Long versionLue) {
        Versions.verifier(versionLue, version, "La fiche de " + identite.nomComplet());
        List<Changement> changements = new ArrayList<>(Changement.identite(identite, nouvelle));
        Changement.comparer(changements, ChampJournal.EMAIL, texte(email), texte(nouvelEmail));
        this.identite = nouvelle;
        this.email = nouvelEmail;
        return changements;
    }

    /** RG-OPE-40 : l'invitation partie d'un contact met son e-mail à jour s'il diffère. */
    public boolean mettreAJourEmail(Email nouvel) {
        if (email != null && email.cle().equals(nouvel.cle())) {
            return false;
        }
        this.email = nouvel;
        return true;
    }

    public boolean aEmail(Email autre) {
        return email != null && autre != null && Objects.equals(email.cle(), autre.cle());
    }

    private static String texte(Email email) {
        return email == null ? null : email.valeur();
    }

    public Long id() {
        return id;
    }

    public long operateur() {
        return operateur;
    }

    public IdentitePersonne identite() {
        return identite;
    }

    /** {@code null} si inconnu. */
    public Email email() {
        return email;
    }

    public long fiche() {
        return fiche;
    }

    public long version() {
        return version;
    }
}
