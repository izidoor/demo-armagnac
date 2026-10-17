package fr.bnia.facturation.application;

import java.time.LocalDate;

/**
 * Le PDF des ventes d'une période (RG-FAC-30, A-FAC-10) : la synthèse par article (quantité, unité, HT), le
 * récapitulatif de TVA et les totaux, édité le jour donné.
 */
public interface EditionVentes {

    byte[] pdf(Ventes ventes, LocalDate editeLe);
}
