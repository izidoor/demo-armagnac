package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.PolitiqueCotisation.Detail;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** La cotisation d'une DSM, telle que la vue la montre (spec 05 §6.3, RG-DEC-32 à 34). */
public sealed interface CotisationDsm {

    /**
     * Calculée à la volée, tant que la DSM n'est pas facturée (RG-DEC-33, 34).
     *
     * @param tva la TVA indicative ; absente si la classe de TVA de la CVO n'a pas de taux à la date du jour
     */
    record Calculee(Detail detail, Optional<TvaIndicative> tva) implements CotisationDsm {

        public Optional<BigDecimal> ttc() {
            return tva.map(t -> detail.totalHt().add(t.montant()));
        }
    }

    /** RG-DEC-32 : aucun taux CVO n'est en vigueur à la fin de la période. */
    record NonCalculable(LocalDate finPeriode) implements CotisationDsm {
    }

    /** RG-DEC-34 : la DSM a au moins une facture ; ses factures et avoirs, par date. */
    record Facturee(List<FactureDsm> documents) implements CotisationDsm {

        public Facturee {
            documents = List.copyOf(documents);
        }

        /** Les factures moins les avoirs. */
        public BigDecimal totalHt() {
            return documents.stream().map(FactureDsm::htSigne).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
        }
    }

    /**
     * RG-DEC-34, H-DEC-22 : part assujettie × taux de la classe de TVA de la CVO à la date du jour, au centime.
     *
     * @param taux pourcentage
     */
    record TvaIndicative(BigDecimal taux, BigDecimal montant) {
    }

    /** Le total HT : calculé, ou facturé ; absent si non calculable. */
    default Optional<BigDecimal> ht() {
        return switch (this) {
            case Calculee c -> Optional.of(c.detail().totalHt());
            case NonCalculable n -> Optional.empty();
            case Facturee f -> Optional.of(f.totalHt());
        };
    }
}
