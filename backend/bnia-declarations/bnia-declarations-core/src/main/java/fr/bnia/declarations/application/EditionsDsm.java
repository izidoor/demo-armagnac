package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.partage.Cote;
import fr.bnia.operateurs.domain.partage.OperateurVue;

/** Production des éditions de la DSM (ADR-020, adapter {@code document}). */
public interface EditionsDsm {

    /**
     * Ce qu'une édition présente (RG-DEC-35) : la DSM, son opérateur, sa cotisation.
     *
     * @param cote le côté du lecteur, pour le libellé de l'état
     */
    record EditionDsm(DeclarationStocksMouvements dsm, OperateurVue operateur, CotisationDsm cotisation, Cote cote) {
    }

    /** RG-DEC-35 : le PDF au format unique, un onglet par page, puis la cotisation. */
    byte[] pdf(EditionDsm edition);
}
