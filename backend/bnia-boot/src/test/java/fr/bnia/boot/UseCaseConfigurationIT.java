package fr.bnia.boot;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.support.AbstractIT;
import fr.bnia.systeme.application.ConsulterInfoSysteme;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.interceptor.TransactionInterceptor;

class UseCaseConfigurationIT extends AbstractIT {

    @Autowired
    ConsulterInfoSysteme casDUsage;

    @Test
    void un_cas_d_usage_est_un_bean_transactionnel() {
        assertThat(AopUtils.isAopProxy(casDUsage)).isTrue();
        assertThat(Arrays.stream(((Advised) casDUsage).getAdvisors()))
                .anyMatch(advisor -> advisor.getAdvice() instanceof TransactionInterceptor);
    }
}
