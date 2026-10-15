package fr.bnia.declarations.application;

import java.util.List;

/**
 * Projection locale des documents de facturation des DSM (A-DEC-4) : brouillons demandés, et factures et
 * avoirs reçus de {@code facturation} par ses événements.
 */
public interface FacturesDsm {

    /**
     * Idempotent : un document déjà reçu (même type, même numéro, ou même brouillon) n'est pas enregistré deux fois.
     *
     * @return {@code true} s'il est nouveau
     */
    boolean inserer(FactureDsm facture);

    /** Le brouillon de cette pièce n'est plus : émise, elle est remplacée par sa facture. */
    void retirerBrouillon(long piece);

    /** Les documents de la DSM : brouillons d'abord, puis par date ; le même jour, les factures avant les avoirs. */
    List<FactureDsm> deLaDsm(long dsm);
}
