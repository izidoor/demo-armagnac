package fr.bnia.facturation.api;

import java.time.LocalDate;
import java.util.List;

/**
 * Une demande de facture, envoyée par une source (spec 08 §3.11, UC-FAC-1) : elle devient un brouillon.
 *
 * @param client         le client de l'hôte, dont l'acheteur se lit par le port {@code Clients} (RG-FAC-11)
 * @param dateOperation  date de résolution des articles (H-FAC-2) : fin de la période d'une DSM
 * @param libellePeriode facultatif ({@code null}) : « DSM de juillet 2026 »
 * @param bonDeCommande  facultatif ({@code null})
 * @param noteEntete     facultative ({@code null}), imprimée au-dessus des lignes
 * @param notePied       facultative ({@code null}), imprimée sous les totaux
 */
public record DemandeFacture(Origine origine, ClientRef client, LocalDate dateOperation, String libellePeriode,
        ConditionPaiement condition, String bonDeCommande, String noteEntete, String notePied,
        List<LigneDemandee> lignes) {

    public DemandeFacture {
        lignes = List.copyOf(lignes);
    }
}
