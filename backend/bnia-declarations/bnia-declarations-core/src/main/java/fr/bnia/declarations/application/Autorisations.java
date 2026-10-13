package fr.bnia.declarations.application;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.Module;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import java.util.Optional;

/**
 * Droits du module « Déclarations » (RG-DEC-01, ADR-016, spec 05 §3.7), évalués à chaque appel par
 * {@code operateurs}. Trois côtés : lire ; agir comme l'opérateur (son propre rôle) ; agir comme le BNIA (le
 * rôle tenu dans l'opérateur BNIA). Partagé par les cas d'usage du contexte.
 */
final class Autorisations {

    private final OperateursLecture operateurs;

    Autorisations(OperateursLecture operateurs) {
        this.operateurs = operateurs;
    }

    /** RG-DEC-01 : lire les DSM, le point de départ et le journal de l'opérateur. */
    OperateurVue exigerLecture(Acteur acteur, long operateur) {
        OperateurVue vue = operateur(operateur);
        if (!droits(acteur, operateur).lit(Module.DECLARATIONS)) {
            throw new AccesRefuse("RG-DEC-01", "Vous n'avez pas accès aux déclarations de " + vue.raisonSociale() + ".");
        }
        return vue;
    }

    /** RG-DEC-01, 21 : agir comme l'opérateur (initier, saisir) ; par son propre rôle, jamais par celui du BNIA. */
    OperateurVue exigerOperateur(Acteur acteur, long operateur) {
        OperateurVue vue = exigerLecture(acteur, operateur);
        Optional<Boolean> gere = droits(acteur, operateur).local().map(r -> r.gere(Module.DECLARATIONS));
        if (gere.isEmpty()) {
            throw new AccesRefuse("RG-DEC-01", "La DSM se remplit par l'opérateur ; le BNIA l'instruit.");
        }
        if (!gere.get()) {
            throw new AccesRefuse("RG-DEC-01", "Votre rôle ne permet pas de gérer les déclarations de "
                    + vue.raisonSociale() + ".");
        }
        return vue;
    }

    /** RG-DEC-01 : action réservée au personnel du BNIA qui gère le module (point de départ, instruction). */
    OperateurVue exigerBnia(Acteur acteur, long operateur) {
        OperateurVue vue = operateur(operateur);
        if (!droits(acteur, operateur).bnia().map(r -> r.gere(Module.DECLARATIONS)).orElse(false)) {
            throw new AccesRefuse("RG-DEC-01",
                    "Action réservée au personnel du BNIA qui gère le module « Déclarations ».");
        }
        return vue;
    }

    /** RG-DEC-01, 22 : gérer le module, comme l'opérateur ou comme le BNIA (supprimer un brouillon). */
    OperateurVue exigerGestion(Acteur acteur, long operateur) {
        OperateurVue vue = exigerLecture(acteur, operateur);
        if (!droits(acteur, operateur).gere(Module.DECLARATIONS)) {
            throw new AccesRefuse("RG-DEC-01", "Votre rôle ne permet pas de gérer les déclarations de "
                    + vue.raisonSociale() + ".");
        }
        return vue;
    }

    private Droits droits(Acteur acteur, long operateur) {
        return operateurs.droits(acteur.utilisateur(), operateur);
    }

    private OperateurVue operateur(long operateur) {
        return operateurs.operateur(operateur)
                .orElseThrow(() -> new ElementIntrouvable("Opérateur introuvable : n° " + operateur + "."));
    }
}
