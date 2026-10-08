package fr.bnia.commun.application;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marque un cas d'usage.
 *
 * <p>{@code bnia-boot} enregistre chaque classe annotée comme bean et exécute ses méthodes
 * publiques dans une transaction. L'annotation est volontairement sans dépendance à Spring
 * (ADR-002).
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UseCase {
}
