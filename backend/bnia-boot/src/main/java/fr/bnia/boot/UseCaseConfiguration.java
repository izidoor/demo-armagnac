package fr.bnia.boot;

import fr.bnia.commun.application.UseCase;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * Câblage des cas d'usage (ADR-002).
 *
 * <p>Les classes {@link UseCase} de {@code bnia-application} ne portent aucune annotation Spring :
 * elles sont enregistrées ici par scan, et chacune de leurs méthodes publiques s'exécute dans une
 * transaction (propagation REQUIRED).
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan(
        basePackages = "fr.bnia",
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = UseCase.class))
class UseCaseConfiguration {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static Advisor transactionUseCaseAdvisor(TransactionManager transactionManager) {
        var interceptor = new TransactionInterceptor(transactionManager, new MatchAlwaysTransactionAttributeSource());
        return new DefaultPointcutAdvisor(new AnnotationMatchingPointcut(UseCase.class, true), interceptor);
    }
}
