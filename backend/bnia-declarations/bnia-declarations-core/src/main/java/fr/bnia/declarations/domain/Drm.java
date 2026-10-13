package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.DocumentDepose;
import java.time.Instant;
import java.time.YearMonth;
import java.util.Objects;

/**
 * DRM d'un producteur pour un mois de la période de sa DSM (entité, spec 05 §3.2.3, RG-DEC-27) : un PDF par
 * mois. Remplacée par une suppression suivie d'un nouveau dépôt (RG-DEC-28).
 */
public record Drm(Long id, long dsm, YearMonth mois, DocumentDepose document, Auteur deposePar, Instant deposeLe) {

    public Drm {
        Objects.requireNonNull(mois, "mois");
        Objects.requireNonNull(document, "document");
        Objects.requireNonNull(deposePar, "deposePar");
        Objects.requireNonNull(deposeLe, "deposeLe");
    }
}
