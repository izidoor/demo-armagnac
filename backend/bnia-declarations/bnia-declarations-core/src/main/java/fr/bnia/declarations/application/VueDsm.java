package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.Anomalie;
import fr.bnia.declarations.domain.CalendrierDeclaratif;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.Manque;
import fr.bnia.declarations.domain.MouvementContrat;
import fr.bnia.declarations.domain.partage.Cote;
import fr.bnia.declarations.domain.partage.EtatDsm;
import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.Module;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Vue d'une DSM (spec 05 §3.6, §6.3) : la DSM, l'opérateur, ses anomalies (RG-DEC-18), ses pièces
 * manquantes (RG-DEC-29), sa cotisation (RG-DEC-31 à 34), les actions permises à l'acteur et son côté, qui
 * décide des libellés d'état, son échéance et son retard (RG-DEC-39, 40), et le brouillon de sa facture
 * demandée (RG-DEC-43).
 *
 * @param contrats           les mouvements de contrat inscrits dans la DSM (spec 06 RG-CA-22, 23)
 * @param envoiPossibleLe    quand l'acteur peut envoyer la DSM mais que sa période n'est pas finie : le lendemain de sa
 *                           fin (spec 06 RG-CA-24) ; « Envoyer » n'est pas offert d'ici là
 * @param brouillonFacture   la pièce de {@code facturation} dont la facture est demandée, pas encore émise
 * @param manquePourFacturer quand l'action « Facturer » est offerte, ce qui manque à l'opérateur pour le facturer
 *                           (RG-OPE-02) : non vide, l'action est refusée (RG-DEC-43) ; vide sinon
 */
public record VueDsm(DeclarationStocksMouvements dsm, OperateurVue operateur, List<Anomalie> anomalies,
        List<Manque> manques, List<MouvementContrat> contrats, CotisationDsm cotisation, Set<ActionDsm> actions, Optional<LocalDate> envoiPossibleLe,
        Cote cote, LocalDate echeance, boolean enRetard, Optional<Long> brouillonFacture,
        List<String> manquePourFacturer) {

    static VueDsm de(DeclarationStocksMouvements dsm, OperateurVue operateur, Droits droits, List<Manque> manques,
            List<MouvementContrat> contrats, CotisationDsm cotisation, Optional<Long> brouillonFacture, Supplier<List<String>> manquePourFacturer,
            LocalDate aujourdhui) {
        Set<ActionDsm> actions = EnumSet.noneOf(ActionDsm.class);
        actions.addAll(ActionDsm.permises(dsm, droits));
        // RG-DEC-43 : validée, sans facture vivante, par qui gère la facturation au BNIA.
        if (dsm.etat() == EtatDsm.VALIDEE
                && !(cotisation instanceof CotisationDsm.Facturee) && brouillonFacture.isEmpty()
                && droits.bnia().map(r -> r.gere(Module.FACTURATION)).orElse(false)) {
            actions.add(ActionDsm.FACTURER);
        }
        Optional<LocalDate> envoiPossibleLe = Optional.empty();
        if (actions.contains(ActionDsm.ENVOYER) && !dsm.envoyablePar(aujourdhui)) {
            actions.remove(ActionDsm.ENVOYER);
            envoiPossibleLe = Optional.of(dsm.envoiPossibleLe());
        }
        return new VueDsm(dsm, operateur, dsm.anomalies(), manques, List.copyOf(contrats), cotisation, actions, envoiPossibleLe,
                Autorisations.cote(droits), dsm.periode().echeance(dsm.type()),
                CalendrierDeclaratif.enRetard(operateur.statutDeclarationStock(), dsm.type(), dsm.periode(),
                        Optional.of(dsm.etat()), aujourdhui), brouillonFacture,
                actions.contains(ActionDsm.FACTURER) ? List.copyOf(manquePourFacturer.get()) : List.of());
    }
}
