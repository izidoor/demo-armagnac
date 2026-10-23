package fr.bnia.declarations.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.declarations.domain.ActionJournalDeclarations;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.MouvementContrat;
import fr.bnia.declarations.domain.partage.RetiraisonValidee;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import fr.bnia.referentiels.domain.partage.RegionVue;
import java.time.Clock;
import java.util.List;

/**
 * Les retiraisons validées des contrats d'achat Armagnac (spec 06 UC-CA-4, A-CA-3), reçues de
 * {@code contratsarmagnac} dans sa transaction : leurs mouvements de contrat et leur inscription dans les DSM.
 * RG-CA-21, 22. Un échec annule la validation de la retiraison.
 */
@UseCase
public class InscrireRetiraisons {

    private final MouvementsContrat mouvements;
    private final DsmRepository dsms;
    private final OperateursLecture operateurs;
    private final ReferentielsLecture referentiels;
    private final Journaliste journaliste;

    public InscrireRetiraisons(MouvementsContrat mouvements, DsmRepository dsms, JournalDeclarations journal,
            OperateursLecture operateurs, ReferentielsLecture referentiels, Clock horloge) {
        this.mouvements = mouvements;
        this.dsms = dsms;
        this.operateurs = operateurs;
        this.referentiels = referentiels;
        this.journaliste = new Journaliste(journal, operateurs, horloge);
    }

    /**
     * RG-CA-21, 22 : un achat pour l'acheteur, une vente pour le vendeur, par ligne ; chacun s'inscrit aussitôt dans
     * la DSM de son opérateur si elle couvre la date d'enlèvement et qu'elle est chez lui (brouillon ou compléments
     * demandés), sinon à l'initiation de cette DSM ({@link GererDsm#initier}).
     */
    public void retiraisonValidee(RetiraisonValidee retiraison) {
        OperateurVue vendeur = operateurs.operateur(retiraison.vendeur()).orElseThrow();
        boolean enRegion = operateurs.operateur(retiraison.acheteur()).orElseThrow().region()
                .flatMap(referentiels::region).map(RegionVue::enRegionDelimitee).orElse(false);
        List<MouvementContrat> crees = mouvements.inserer(MouvementContrat.de(retiraison, vendeur.type(), enRegion));
        Acteur validePar = new Acteur(retiraison.validePar());
        for (long operateur : List.of(retiraison.acheteur(), retiraison.vendeur())) {
            dsms.nonValideeDe(operateur)
                    .filter(d -> d.etat().modifiable())
                    .filter(d -> !retiraison.dateEnlevement().isBefore(d.periode().debut())
                            && !retiraison.dateEnlevement().isAfter(d.periode().fin()))
                    .ifPresent(dsm -> {
                        List<MouvementContrat> inscrits = crees.stream()
                                .filter(m -> m.operateur() == operateur)
                                .filter(dsm::inscrire)
                                .toList();
                        if (!inscrits.isEmpty()) {
                            DeclarationStocksMouvements enregistree = dsms.mettreAJour(dsm);
                            inscrits.forEach(m -> mouvements.inscrire(m.id(), enregistree.id()));
                            journaliste.noter(validePar, enregistree, ActionJournalDeclarations.CONTRAT_INSCRIT,
                                    inscrits.getFirst().libelle());
                        }
                    });
        }
    }
}
