package fr.bnia.contratsarmagnac.application;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.contratsarmagnac.domain.ContratAchatArmagnac;
import fr.bnia.contratsarmagnac.domain.Partie;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.Module;

/**
 * Droits du module « Contrats » (RG-CA-01, ADR-016 amendé le 2026-10-23), évalués à chaque appel par
 * {@code operateurs} : une partie lit et, selon son rôle, agit, par son opérateur actif ; le BNIA lit tout et n'agit
 * sur rien. Partagé par les cas d'usage du contexte.
 */
final class Autorisations {

    private final OperateursLecture operateurs;

    Autorisations(OperateursLecture operateurs) {
        this.operateurs = operateurs;
    }

    /** RG-CA-01 : lire les contrats de l'opérateur, au portail. */
    void exigerLecture(Acteur acteur, long operateur) {
        if (!droits(acteur, operateur).lit(Module.CONTRATS)) {
            throw new AccesRefuse("RG-CA-01", "Vous n'avez pas accès aux contrats de cet opérateur.");
        }
    }

    /** RG-CA-01 : agir comme l'opérateur, par son propre rôle ; jamais pour le BNIA. */
    void exigerGestion(Acteur acteur, long operateur) {
        if (operateur == operateurs.operateurBnia()) {
            throw new AccesRefuse("RG-CA-01", "Le BNIA consulte les contrats ; seules les parties y agissent.");
        }
        exigerLecture(acteur, operateur);
        if (!droits(acteur, operateur).gere(Module.CONTRATS)) {
            throw new AccesRefuse("RG-CA-01", "Votre rôle ne permet que de lire les contrats.");
        }
    }

    /** RG-CA-01 : tout le personnel du BNIA lit tous les contrats, quel que soit son rôle. */
    void exigerPersonnelBnia(Acteur acteur) {
        if (!droits(acteur, operateurs.operateurBnia()).personnelBnia()) {
            throw new AccesRefuse("RG-CA-01", "Liste réservée au personnel du BNIA.");
        }
    }

    /** RG-CA-01 : la partie que l'opérateur tient dans le contrat ; un contrat d'autres opérateurs est introuvable. */
    static Partie partie(ContratAchatArmagnac contrat, long operateur) {
        return contrat.partieDe(operateur)
                .orElseThrow(() -> new ElementIntrouvable("Contrat introuvable."));
    }

    private Droits droits(Acteur acteur, long operateur) {
        return operateurs.droits(acteur.utilisateur(), operateur);
    }
}
