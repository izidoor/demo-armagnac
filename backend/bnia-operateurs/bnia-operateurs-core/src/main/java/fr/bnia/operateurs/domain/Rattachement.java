package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.partage.Role;
import java.time.Instant;

/**
 * Rattachement (vision §2.2, spec 02 §3.2.4) : l'accès d'un utilisateur à un opérateur, avec un rôle.
 * Racine de son agrégat (A-OPE-6) ; au plus un par opérateur et utilisateur (RG-OPE-29). Rôle et statut
 * appartiennent aux ADMIN de l'opérateur ; le dernier ADMIN est protégé par le cas d'usage (RG-OPE-35).
 */
public final class Rattachement {

    private final Long id;
    private final long operateur;
    private final long utilisateur;
    private Role role;
    private StatutRattachement statut;
    private final long fiche;
    private final Instant creeLe;

    private Rattachement(Long id, long operateur, long utilisateur, Role role, StatutRattachement statut, long fiche,
            Instant creeLe) {
        this.id = id;
        this.operateur = operateur;
        this.utilisateur = utilisateur;
        this.role = exigerRole(role);
        this.statut = statut;
        this.fiche = fiche;
        this.creeLe = creeLe;
    }

    /** RG-OPE-42 : actif, avec le rôle de l'invitation et la fiche de la personne. */
    public static Rattachement creer(long operateur, long utilisateur, Role role, long fiche, Instant maintenant) {
        return new Rattachement(null, operateur, utilisateur, role, StatutRattachement.ACTIF, fiche, maintenant);
    }

    public static Rattachement reconstituer(Long id, long operateur, long utilisateur, Role role,
            StatutRattachement statut, long fiche, Instant creeLe) {
        return new Rattachement(id, operateur, utilisateur, role, statut, fiche, creeLe);
    }

    /** RG-OPE-43. */
    public void changerRole(Role nouveau) {
        Role verifie = exigerRole(nouveau);
        if (verifie == role) {
            throw new RegleEnfreinte("RG-OPE-43", "Ce membre a déjà le rôle « " + role.libelle() + " ».");
        }
        this.role = verifie;
    }

    /** RG-OPE-43 : l'accès à cet opérateur seul est suspendu. */
    public void suspendre() {
        if (statut == StatutRattachement.SUSPENDU) {
            throw new RegleEnfreinte("RG-OPE-43", "L'accès de ce membre est déjà suspendu.");
        }
        this.statut = StatutRattachement.SUSPENDU;
    }

    /** RG-OPE-43. */
    public void reactiver() {
        if (statut == StatutRattachement.ACTIF) {
            throw new RegleEnfreinte("RG-OPE-43", "L'accès de ce membre est déjà actif.");
        }
        this.statut = StatutRattachement.ACTIF;
    }

    /** RG-OPE-35 : compte parmi les ADMIN actifs de l'opérateur. */
    public boolean estAdminActif() {
        return role == Role.ADMIN && statut == StatutRattachement.ACTIF;
    }

    public boolean estActif() {
        return statut == StatutRattachement.ACTIF;
    }

    private static Role exigerRole(Role role) {
        if (role == null) {
            throw new RegleEnfreinte("RG-OPE-43", "Le rôle est obligatoire : observateur, agent ou admin.");
        }
        return role;
    }

    public Long id() {
        return id;
    }

    public long operateur() {
        return operateur;
    }

    public long utilisateur() {
        return utilisateur;
    }

    public Role role() {
        return role;
    }

    public StatutRattachement statut() {
        return statut;
    }

    public long fiche() {
        return fiche;
    }

    public Instant creeLe() {
        return creeLe;
    }
}
