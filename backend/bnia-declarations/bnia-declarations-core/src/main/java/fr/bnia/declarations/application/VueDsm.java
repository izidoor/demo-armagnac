package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.Anomalie;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import java.util.List;

/**
 * Vue d'une DSM (spec 05 §3.6, §6.3) : la DSM, l'opérateur et ses anomalies (RG-DEC-18). La cotisation
 * vient avec le lot 4, les pièces manquantes avec le lot 3.
 */
public record VueDsm(DeclarationStocksMouvements dsm, OperateurVue operateur, List<Anomalie> anomalies) {

    static VueDsm de(DeclarationStocksMouvements dsm, OperateurVue operateur) {
        return new VueDsm(dsm, operateur, dsm.anomalies());
    }
}
