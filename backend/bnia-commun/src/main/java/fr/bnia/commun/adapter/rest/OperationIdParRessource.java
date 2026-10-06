package fr.bnia.commun.adapter.rest;

import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

/**
 * Nomme les opérations héritées d'un controller générique (ADR-007). Sans cela, springdoc reprend le
 * nom de la méthode et suffixe les doublons ({@code lister_1}), illisibles dans le client généré.
 *
 * <p>Nom produit : méthode + ressource, au pluriel pour {@code lister}, au singulier sinon.
 */
@Component
class OperationIdParRessource implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        Class<?> controller = handlerMethod.getBeanType();
        Ressource ressource = AnnotatedElementUtils.findMergedAnnotation(controller, Ressource.class);
        String methode = handlerMethod.getMethod().getName();
        if (ressource != null && handlerMethod.getMethod().getDeclaringClass() != controller) {
            operation.setOperationId(methode
                    + (methode.equals("lister") ? ressource.pluriel() : ressource.singulier()));
        }
        return operation;
    }
}
