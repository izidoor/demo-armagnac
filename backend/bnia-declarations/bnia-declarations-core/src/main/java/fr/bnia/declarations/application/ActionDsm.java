package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.partage.EtatDsm;
import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.Module;
import java.util.EnumSet;
import java.util.Set;

/**
 * Les actions qu'un acteur peut faire sur une DSM, selon son état et son côté (spec 05 §6.3, RG-DEC-01, 20 à
 * 22, 24, 28, 43) : l'écran n'affiche que celles-là. Écrire dans la conversation de la DSM relève de
 * {@code messagerie}, qui dit elle-même si l'acteur y écrit (RG-MSG-01). Les cas d'usage revérifient chacune.
 */
public enum ActionDsm {

    ENREGISTRER,
    ENVOYER,
    SUPPRIMER,
    PRENDRE_EN_INSTRUCTION,
    DEMANDER_COMPLEMENTS,
    VALIDER,
    /** RG-DEC-24 : tenir le carnet de notes internes (personnel du BNIA qui gère le module). */
    ANNOTER,
    DEPOSER_PIECES,
    /** RG-DEC-43 : DSM validée sans facture vivante, acteur qui gère la facturation au BNIA. */
    FACTURER;

    static Set<ActionDsm> permises(DeclarationStocksMouvements dsm, Droits droits) {
        Set<ActionDsm> actions = EnumSet.noneOf(ActionDsm.class);
        EtatDsm etat = dsm.etat();
        if (droits.local().map(r -> r.gere(Module.DECLARATIONS)).orElse(false)) {
            if (etat.modifiable()) {
                actions.add(ENREGISTRER);
                actions.add(ENVOYER);
                actions.add(DEPOSER_PIECES);
            }
            if (etat == EtatDsm.BROUILLON) {
                actions.add(SUPPRIMER);
            }
        } else if (droits.bnia().map(r -> r.gere(Module.DECLARATIONS)).orElse(false)) {
            actions.add(ANNOTER);
            if (etat != EtatDsm.VALIDEE) {
                actions.add(DEPOSER_PIECES);
            }
            switch (etat) {
                case BROUILLON -> actions.add(SUPPRIMER);
                case ENVOYEE -> actions.add(PRENDRE_EN_INSTRUCTION);
                case EN_INSTRUCTION, COMPLEMENTS_RECUS -> {
                    actions.add(DEMANDER_COMPLEMENTS);
                    actions.add(VALIDER);
                }
                case COMPLEMENTS_DEMANDES, VALIDEE -> {
                }
            }
        }
        return actions;
    }
}
