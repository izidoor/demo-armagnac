package fr.bnia.facturation.domain;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Le suivi d'une pièce émise dans Zeendoc (spec 08 §3.10, RG-FAC-32, 33) : transmise à un instant, puis payée
 * à une date.
 *
 * @param payeeLe vide tant qu'elle n'est pas payée
 */
public record SuiviZeendoc(EtatZeendoc etat, Instant transmiseLe, LocalDate payeeLe) {

    static SuiviZeendoc transmise(Instant le) {
        return new SuiviZeendoc(EtatZeendoc.TRANSMISE, le, null);
    }

    SuiviZeendoc payee(LocalDate le) {
        return new SuiviZeendoc(EtatZeendoc.PAYEE, transmiseLe, le);
    }
}
