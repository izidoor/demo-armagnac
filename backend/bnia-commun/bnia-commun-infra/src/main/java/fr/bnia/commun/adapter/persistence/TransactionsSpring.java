package fr.bnia.commun.adapter.persistence;

import fr.bnia.commun.application.Transactions;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/** Le port {@link Transactions} (ADR-022) : {@link TransactionTemplate} en propagation {@code REQUIRES_NEW}. */
@Component
class TransactionsSpring implements Transactions {

    private final TransactionTemplate template;

    TransactionsSpring(PlatformTransactionManager transactionManager) {
        this.template = new TransactionTemplate(transactionManager);
        this.template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public <T> T dansUneNouvelle(Supplier<T> action) {
        return template.execute(statut -> action.get());
    }
}
