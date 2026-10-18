package fr.bnia.facturation.application;

/**
 * Port vers Zeendoc (spec 08 §3.10, RG-FAC-32), la GED du BNIA : implémenté dans l'infra par un adapter simulé
 * pour le prototype (bordure, {@code contextes.md} §1.1).
 */
public interface Zeendoc {

    /** Dépose le PDF d'une pièce émise ; son numéro sert de clé d'idempotence. */
    void deposer(String numero, byte[] pdf);
}
