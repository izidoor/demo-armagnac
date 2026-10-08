package fr.bnia.boot;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.referentiels.application.GererValeursSimples;
import fr.bnia.referentiels.application.GererZonesCommerciales;
import fr.bnia.support.AbstractIT;
import fr.bnia.systeme.application.ConsulterInfoSysteme;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.aop.PointcutAdvisor;
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

    @Autowired
    GererZonesCommerciales casDUsageHerite;

    @Test
    void les_methodes_heritees_d_un_cas_d_usage_sont_transactionnelles() throws NoSuchMethodException {
        var creer = GererValeursSimples.class.getMethod("creer", long.class, String.class, String.class);

        assertThat(Arrays.stream(((Advised) casDUsageHerite).getAdvisors()))
                .filteredOn(advisor -> advisor.getAdvice() instanceof TransactionInterceptor)
                .singleElement()
                .satisfies(advisor -> assertThat(((PointcutAdvisor) advisor).getPointcut().getMethodMatcher()
                        .matches(creer, GererZonesCommerciales.class)).isTrue());
    }
}
