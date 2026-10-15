package fr.bnia.facturation.application;

import fr.bnia.facturation.domain.Numerotation;

/** Le compteur des numéros de pièce par année (spec 08 §3.8, A-FAC-5). */
public interface NumerotationRepository {

    /**
     * RG-FAC-12 : le compteur de l'année, créé au besoin, verrouillé jusqu'à la fin de la transaction : deux
     * émissions simultanées s'attendent, et un rollback ne consomme pas de numéro.
     */
    Numerotation verrouiller(int annee);

    void enregistrer(Numerotation numerotation);
}
