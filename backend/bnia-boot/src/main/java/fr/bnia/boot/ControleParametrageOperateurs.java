package fr.bnia.boot;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Module;
import fr.bnia.referentiels.application.ControleParametrage;
import org.springframework.stereotype.Component;

/**
 * Les droits de {@code operateurs} sur le Paramétrage, pour les cas d'usage de {@code referentiels}, qui
 * ne peuvent lire {@code operateurs} sans cycle (ce dernier lit les référentiels). Câblage seulement :
 * la règle est celle de {@code Droits} (RG-OPE-31, 32).
 */
@Component
class ControleParametrageOperateurs implements ControleParametrage {

    private final OperateursLecture operateurs;

    ControleParametrageOperateurs(OperateursLecture operateurs) {
        this.operateurs = operateurs;
    }

    @Override
    public void exigerGestion(long utilisateur) {
        if (!operateurs.droits(utilisateur, operateurs.operateurBnia()).gere(Module.PARAMETRAGE)) {
            throw new AccesRefuse("RG-OPE-31", "Le Paramétrage est réservé aux agents et aux administrateurs du BNIA.");
        }
    }

    @Override
    public void exigerGestionFiche(long utilisateur, long operateur) {
        if (!operateurs.droits(utilisateur, operateur).gere(Module.CONTACTS)) {
            throw new AccesRefuse("RG-OPE-31", "Votre rôle ne permet pas de gérer la fiche de cet opérateur.");
        }
    }
}
