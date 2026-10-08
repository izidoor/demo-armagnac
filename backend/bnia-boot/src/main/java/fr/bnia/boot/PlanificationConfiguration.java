package fr.bnia.boot;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Tâches planifiées des contextes (expiration des invitations, RG-OPE-41). */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
class PlanificationConfiguration {
}
