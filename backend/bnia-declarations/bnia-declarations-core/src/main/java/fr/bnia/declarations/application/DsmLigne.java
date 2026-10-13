package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.EtatDsm;
import fr.bnia.declarations.domain.PeriodeDeclaration;
import fr.bnia.declarations.domain.TypeDeclarant;
import java.time.Instant;

/**
 * Une DSM dans un listing (spec 05 §6.1, §6.2).
 *
 * @param modifieeLe date de la dernière action sur la DSM, lue au journal (spec 05 §3.2 : la DSM ne porte pas de
 *                   « dernière modification ») ; absente sans entrée
 */
public record DsmLigne(long id, long operateur, TypeDeclarant type, PeriodeDeclaration periode, EtatDsm etat,
        Instant envoyeeLe, Instant valideeLe, Instant modifieeLe) {
}
