package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.EtatDsm;
import fr.bnia.declarations.domain.PeriodeDeclaration;
import fr.bnia.declarations.domain.TypeDeclarant;
import java.time.Instant;

/** Une DSM dans un listing (spec 05 §6.1, §6.2). */
public record DsmLigne(long id, long operateur, TypeDeclarant type, PeriodeDeclaration periode, EtatDsm etat,
        Instant envoyeeLe, Instant valideeLe) {
}
