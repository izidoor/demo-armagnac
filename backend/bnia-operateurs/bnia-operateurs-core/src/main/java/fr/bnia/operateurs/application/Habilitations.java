package fr.bnia.operateurs.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.operateurs.domain.Rattachement;
import fr.bnia.operateurs.domain.Utilisateur;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.EtatConnexion;
import fr.bnia.operateurs.domain.partage.Module;
import fr.bnia.operateurs.domain.partage.Role;
import java.util.List;
import java.util.Optional;

/**
 * Droits d'un acteur sur un opérateur, évalués à chaque appel (RG-OPE-31 à 33, ADR-016) : utilisés par
 * chaque cas d'usage du contexte, publiés aux autres par {@code OperateursLecture}, lus par le front
 * (« mes droits »). Et « cet utilisateur peut-il se connecter ? » pour {@code authentification}
 * (RG-AUT-04).
 */
@UseCase
public class Habilitations {

    private final UtilisateurRepository utilisateurs;
    private final RattachementRepository rattachements;
    private final OperateurRepository operateurs;

    public Habilitations(UtilisateurRepository utilisateurs, RattachementRepository rattachements,
            OperateurRepository operateurs) {
        this.utilisateurs = utilisateurs;
        this.rattachements = rattachements;
        this.operateurs = operateurs;
    }

    /**
     * RG-OPE-31, 32 : le rôle du rattachement {@code ACTIF} dans l'opérateur (s'il est actif) et celui du
     * rattachement {@code ACTIF} dans l'opérateur BNIA. Un utilisateur bloqué n'a aucun droit (RG-OPE-50).
     */
    public Droits droits(Acteur acteur, long operateur) {
        Optional<Utilisateur> utilisateur = utilisateurs.parId(acteur.utilisateur());
        if (utilisateur.isEmpty() || utilisateur.get().estBloque()) {
            return Droits.aucun(operateur);
        }
        List<Rattachement> siens = rattachements.deLUtilisateur(acteur.utilisateur());
        long bnia = operateurs.idBnia();
        Optional<Role> roleBnia = actif(siens, bnia);
        Optional<Role> local = operateur == bnia ? roleBnia
                : actif(siens, operateur).filter(r -> operateurs.estActif(operateur));
        return new Droits(operateur, local, roleBnia);
    }

    /** Les droits de l'acteur sur l'opérateur BNIA : ceux qu'il tient du personnel du BNIA (RG-OPE-32). */
    public Droits droitsBnia(Acteur acteur) {
        return droits(acteur, operateurs.idBnia());
    }

    /** RG-AUT-04, 09 : non bloqué, au moins un rattachement {@code ACTIF} dans un opérateur actif. */
    public EtatConnexion etatConnexion(long utilisateur) {
        Optional<Utilisateur> trouve = utilisateurs.parId(utilisateur);
        if (trouve.isEmpty()) {
            return EtatConnexion.INCONNU;
        }
        if (trouve.get().estBloque()) {
            return EtatConnexion.UTILISATEUR_BLOQUE;
        }
        boolean utilisable = rattachements.deLUtilisateur(utilisateur).stream()
                .anyMatch(r -> r.estActif() && operateurs.estActif(r.operateur()));
        return utilisable ? EtatConnexion.AUTORISE : EtatConnexion.AUCUN_ACCES_ACTIF;
    }

    /** RG-OPE-31 : lire un module de l'opérateur ; refusé sinon. */
    public Droits exigerLecture(Acteur acteur, long operateur, Module module) {
        Droits droits = droits(acteur, operateur);
        if (!droits.lit(module)) {
            throw new AccesRefuse("RG-OPE-31", "Vous n'avez pas accès à cet opérateur.");
        }
        return droits;
    }

    /** RG-OPE-31 : gérer un module de l'opérateur ; refusé, en nommant le module, sinon. */
    public Droits exigerGestion(Acteur acteur, long operateur, Module module) {
        Droits droits = droits(acteur, operateur);
        if (!droits.gere(module)) {
            throw new AccesRefuse("RG-OPE-31", droits.aucunAcces()
                    ? "Vous n'avez pas accès à cet opérateur."
                    : "Votre rôle ne permet pas de gérer le module « " + module.libelle() + " » de cet opérateur.");
        }
        return droits;
    }

    /** RG-OPE-32 : action réservée au personnel du BNIA, avec le module donné en lecture ou en gestion. */
    public Droits exigerBnia(Acteur acteur, Module module, boolean gestion) {
        Droits droits = droitsBnia(acteur);
        boolean permis = gestion ? droits.gere(module) : droits.lit(module);
        if (!droits.personnelBnia() || !permis) {
            throw new AccesRefuse("RG-OPE-32", "Action réservée au personnel du BNIA"
                    + (gestion ? " qui gère le module « " + module.libelle() + " »." : "."));
        }
        return droits;
    }

    /** RG-OPE-25, 26, 32, 50 : action réservée à un ADMIN de l'opérateur BNIA. */
    public Droits exigerAdminBnia(Acteur acteur) {
        Droits droits = droitsBnia(acteur);
        if (!droits.adminBnia()) {
            throw new AccesRefuse("RG-OPE-32", "Action réservée à un ADMIN du BNIA.");
        }
        return droits;
    }

    private static Optional<Role> actif(List<Rattachement> rattachements, long operateur) {
        return rattachements.stream()
                .filter(r -> r.operateur() == operateur && r.estActif())
                .map(Rattachement::role)
                .findFirst();
    }
}
