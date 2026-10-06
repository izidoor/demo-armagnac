package fr.bnia.commun.adapter.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Nom de la ressource d'un controller dont les opérations sont héritées d'une classe générique.
 * Sert à nommer ces opérations dans le contrat OpenAPI : {@code listerZonesCommerciales},
 * {@code creerZoneCommerciale} (voir {@link OperationIdParRessource}).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Ressource {

    /** Nom de la ressource au singulier, en PascalCase ({@code ZoneCommerciale}). */
    String singulier();

    /** Nom de la ressource au pluriel, en PascalCase ({@code ZonesCommerciales}). */
    String pluriel();
}
