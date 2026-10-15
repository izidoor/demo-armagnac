package fr.bnia.declarations.application;

import fr.bnia.operateurs.domain.partage.Acteur;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Port de {@code declarations} vers {@code facturation} (ADR-021) : demander les factures de DSM, dans les
 * termes de ce contexte ; implémenté dans son infra par un adapter sur l'API de {@code facturation}.
 */
public interface Facturation {

    /**
     * RG-FAC-01 : un brouillon par demande, tout ou rien.
     *
     * @return l'identifiant du brouillon de chaque demande, dans leur ordre
     */
    List<Long> demander(Acteur acteur, List<DemandeFactureDsm> demandes);

    /**
     * La demande de facture d'une DSM (spec 08 §3.13).
     *
     * @param reference      « DSM juillet 2026 »
     * @param libellePeriode « DSM de juillet 2026 »
     * @param dateOperation  fin de la période : date des articles (H-FAC-2)
     * @param joursPaiement  H-FAC-3 : 30
     * @param finDeMois      H-FAC-3 : oui
     * @param notePied       H-FAC-6
     */
    record DemandeFactureDsm(long dsm, long operateur, String codeOperateur, String reference, String libellePeriode,
            LocalDate dateOperation, int joursPaiement, boolean finDeMois, List<Groupe> groupes, String notePied) {

        public DemandeFactureDsm {
            groupes = List.copyOf(groupes);
        }
    }

    /** Un groupe de la facture : une famille de lignes de cotisation (commerce de l'Armagnac, EDV). */
    record Groupe(String titre, List<LigneArticle> lignes) {

        public Groupe {
            lignes = List.copyOf(lignes);
        }
    }

    /**
     * Une ligne article : un article du catalogue de la cotisation et sa quantité.
     *
     * @param quantite hlAP
     */
    record LigneArticle(String code, BigDecimal quantite) {
    }
}
