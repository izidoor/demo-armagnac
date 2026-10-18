package fr.bnia.facturation.api;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Une facture a été émise (spec 08 §3.11), publiée dans la transaction de l'émission (RG-FAC-25, ADR-013). La
 * source s'y reconnaît par l'origine.
 *
 * @param piece     identifiant de la pièce dans {@code facturation}
 * @param client    {@code null} pour un client libre
 * @param emisePar  l'utilisateur qui l'a émise, pour le journal de la source
 */
public record FactureEmise(long piece, Origine origine, ClientRef client, String numero, LocalDate date,
        BigDecimal ht, BigDecimal tva, BigDecimal ttc, Acteur emisePar) {
}
