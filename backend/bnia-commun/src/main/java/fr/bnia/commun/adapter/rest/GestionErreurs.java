package fr.bnia.commun.adapter.rest;

import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.ElementUtilise;
import fr.bnia.commun.domain.RegleEnfreinte;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduit les exceptions métier en {@link ProblemDetail} (RFC 9457). Le {@code detail} est le message
 * destiné à l'utilisateur ; la propriété {@code regle} porte l'identifiant de la règle enfreinte
 * ({@code RG-REF-11}) quand il existe.
 */
@RestControllerAdvice
class GestionErreurs {

    @ExceptionHandler(ConflitUnicite.class)
    ProblemDetail conflit(ConflitUnicite exception) {
        return probleme(HttpStatus.CONFLICT, "Conflit", exception);
    }

    @ExceptionHandler(ElementUtilise.class)
    ProblemDetail utilise(ElementUtilise exception) {
        return probleme(HttpStatus.CONFLICT, "Valeur utilisée", exception);
    }

    @ExceptionHandler(RegleEnfreinte.class)
    ProblemDetail regleEnfreinte(RegleEnfreinte exception) {
        return probleme(HttpStatus.UNPROCESSABLE_CONTENT, "Règle de gestion enfreinte", exception);
    }

    @ExceptionHandler(ElementIntrouvable.class)
    ProblemDetail introuvable(ElementIntrouvable exception) {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        probleme.setTitle("Introuvable");
        return probleme;
    }

    private static ProblemDetail probleme(HttpStatus statut, String titre, RegleEnfreinte exception) {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(statut, exception.getMessage());
        probleme.setTitle(titre);
        exception.regle().ifPresent(regle -> probleme.setProperty("regle", regle));
        return probleme;
    }
}
