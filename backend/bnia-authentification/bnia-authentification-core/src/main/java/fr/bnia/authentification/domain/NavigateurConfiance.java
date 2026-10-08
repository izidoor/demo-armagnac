package fr.bnia.authentification.domain;

import java.time.Instant;

/**
 * Navigateur de confiance (spec 03 §3.4, RG-AUT-06) : le jeton de son cookie, en empreinte ; il dispense
 * du code jusqu'à son expiration.
 */
public record NavigateurConfiance(String empreinte, Instant creeLe, Instant expireLe) {
}
