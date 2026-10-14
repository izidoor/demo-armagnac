package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.Anomalie;
import fr.bnia.declarations.domain.CalendrierDeclaratif;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.Manque;
import fr.bnia.declarations.domain.partage.Cote;
import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Vue d'une DSM (spec 05 §3.6, §6.3) : la DSM, l'opérateur, ses anomalies (RG-DEC-18), ses pièces
 * manquantes (RG-DEC-29), sa cotisation (RG-DEC-31 à 34), les actions permises à l'acteur et son côté, qui
 * décide des libellés d'état, son échéance et son retard (RG-DEC-39, 40).
 */
public record VueDsm(DeclarationStocksMouvements dsm, OperateurVue operateur, List<Anomalie> anomalies,
        List<Manque> manques, CotisationDsm cotisation, Set<ActionDsm> actions, Cote cote, LocalDate echeance,
        boolean enRetard) {

    static VueDsm de(DeclarationStocksMouvements dsm, OperateurVue operateur, Droits droits, List<Manque> manques,
            CotisationDsm cotisation, LocalDate aujourdhui) {
        return new VueDsm(dsm, operateur, dsm.anomalies(), manques, cotisation, ActionDsm.permises(dsm, droits),
                Autorisations.cote(droits), dsm.periode().echeance(dsm.type()),
                CalendrierDeclaratif.enRetard(operateur.statutDeclarationStock(), dsm.type(), dsm.periode(),
                        Optional.of(dsm.etat()), aujourdhui));
    }
}
