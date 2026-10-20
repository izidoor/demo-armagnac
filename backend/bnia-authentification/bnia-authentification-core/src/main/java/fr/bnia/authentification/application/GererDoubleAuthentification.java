package fr.bnia.authentification.application;

import fr.bnia.authentification.domain.CompteConnexion;
import fr.bnia.authentification.domain.EvenementConnexion;
import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.Module;
import java.time.Clock;

/**
 * RG-AUT-16 : la double authentification, pour toute l'application, activée ou désactivée par un ADMIN
 * du BNIA ; le personnel du BNIA la consulte. Chaque changement va au journal des connexions.
 */
@UseCase
public class GererDoubleAuthentification {

    private final ReglageConnexion reglage;
    private final CompteRepository comptes;
    private final OperateursLecture operateurs;
    private final Greffe greffe;

    public GererDoubleAuthentification(ReglageConnexion reglage, CompteRepository comptes,
            OperateursLecture operateurs, JournalConnexions journal, EnvoiCourriels envoi,
            ParametresAuthentification parametres, Clock horloge) {
        this.reglage = reglage;
        this.comptes = comptes;
        this.operateurs = operateurs;
        this.greffe = new Greffe(journal, envoi, parametres, horloge);
    }

    /** RG-AUT-16 : un rôle de l'opérateur BNIA (module Accès, lecture). */
    public EtatDoubleAuthentification consulter(long acteur) {
        Droits droits = droits(acteur);
        if (!droits.lit(Module.ACCES)) {
            throw new AccesRefuse("RG-AUT-16", "Le réglage de la double authentification est réservé au personnel"
                    + " du BNIA.");
        }
        return new EtatDoubleAuthentification(reglage.doubleAuthentification(), droits.adminBnia());
    }

    /** RG-AUT-16, 12 : un ADMIN du BNIA ; sans effet si la valeur ne change pas. */
    public EtatDoubleAuthentification definir(long acteur, boolean active, Contexte contexte) {
        if (!droits(acteur).adminBnia()) {
            throw new AccesRefuse("RG-AUT-16", "Seul un administrateur du BNIA active ou désactive la double"
                    + " authentification.");
        }
        if (reglage.doubleAuthentification() != active) {
            reglage.definirDoubleAuthentification(active);
            String email = comptes.parUtilisateur(acteur).map(CompteConnexion::identifiant).orElse("");
            greffe.noter(email, acteur, active ? EvenementConnexion.DOUBLE_AUTHENTIFICATION_ACTIVEE
                    : EvenementConnexion.DOUBLE_AUTHENTIFICATION_DESACTIVEE, null, contexte);
        }
        return new EtatDoubleAuthentification(active, true);
    }

    private Droits droits(long acteur) {
        return operateurs.droits(acteur, operateurs.operateurBnia());
    }
}
