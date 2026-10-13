package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.Anomalie;
import fr.bnia.declarations.domain.Cote;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.Manque;
import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import java.util.List;
import java.util.Set;

/**
 * Vue d'une DSM (spec 05 §3.6, §6.3) : la DSM, l'opérateur, ses anomalies (RG-DEC-18), ses pièces
 * manquantes (RG-DEC-29), sa cotisation (RG-DEC-31 à 34), les actions permises à l'acteur et son côté, qui
 * décide des libellés d'état.
 */
public record VueDsm(DeclarationStocksMouvements dsm, OperateurVue operateur, List<Anomalie> anomalies,
        List<Manque> manques, CotisationDsm cotisation, Set<ActionDsm> actions, Cote cote) {

    static VueDsm de(DeclarationStocksMouvements dsm, OperateurVue operateur, Droits droits, List<Manque> manques,
            CotisationDsm cotisation) {
        return new VueDsm(dsm, operateur, dsm.anomalies(), manques, cotisation, ActionDsm.permises(dsm, droits),
                Autorisations.cote(droits));
    }
}
