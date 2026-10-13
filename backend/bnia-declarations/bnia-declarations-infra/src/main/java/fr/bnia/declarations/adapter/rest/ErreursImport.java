package fr.bnia.declarations.adapter.rest;

import fr.bnia.declarations.adapter.rest.ImportRefuseResponse.ErreurImportResponse;
import fr.bnia.declarations.domain.ImportRefuse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * RG-DEC-37 : un import refusé rend, en plus du problème habituel (422, propriété {@code regle}), la liste de ses
 * erreurs situées. Passe avant le traitement commun des règles enfreintes.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class ErreursImport {

    @ExceptionHandler(ImportRefuse.class)
    ProblemDetail importRefuse(ImportRefuse exception) {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessage());
        probleme.setTitle("Import refusé");
        exception.regle().ifPresent(regle -> probleme.setProperty("regle", regle));
        probleme.setProperty("erreurs", exception.erreurs().stream().map(ErreurImportResponse::depuis).toList());
        return probleme;
    }
}
