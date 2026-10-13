package fr.bnia.declarations.application;

import java.util.List;

/** Projection locale des factures et avoirs des DSM, alimentée par les événements de {@code facturation} (A-DEC-4). */
public interface FacturesDsm {

    /** Idempotent : un document déjà reçu (même type, même numéro) n'est pas enregistré deux fois. */
    void inserer(FactureDsm facture);

    /** Les factures et avoirs de la DSM, par date ; le même jour, les factures avant les avoirs. */
    List<FactureDsm> deLaDsm(long dsm);
}
