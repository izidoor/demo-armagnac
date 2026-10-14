package fr.bnia.declarations.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.declarations.domain.partage.AvoirEmis;
import fr.bnia.declarations.domain.partage.FactureEmise;

/**
 * Projection locale des factures et avoirs des DSM (A-DEC-4, RG-DEC-34), alimentée par les événements de
 * {@code facturation}, dans sa transaction (ADR-013). Sans acteur : aucun droit à vérifier.
 */
@UseCase
public class EnregistrerFactures {

    private final FacturesDsm factures;

    public EnregistrerFactures(FacturesDsm factures) {
        this.factures = factures;
    }

    /** RG-DEC-34 : la DSM devient facturée ; un même numéro reçu deux fois n'est enregistré qu'une fois. */
    public void factureEmise(FactureEmise facture) {
        factures.inserer(new FactureDsm(facture.dsm(), FactureDsm.Type.FACTURE, facture.numero(), facture.date(),
                facture.ht(), facture.tva(), facture.ttc()));
    }

    /** RG-DEC-34 : l'avoir se retranche du total facturé de la DSM. */
    public void avoirEmis(AvoirEmis avoir) {
        factures.inserer(new FactureDsm(avoir.dsm(), FactureDsm.Type.AVOIR, avoir.numero(), avoir.date(), avoir.ht(),
                avoir.tva(), avoir.ttc()));
    }
}
