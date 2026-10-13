package fr.bnia.declarations.application;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.EtatDsm;
import fr.bnia.declarations.domain.PeriodeDeclaration;
import fr.bnia.declarations.domain.TypeDeclarant;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Une DSM dans un listing (spec 05 §6.1, §6.2).
 *
 * @param modifieeLe date de la dernière action sur la DSM, lue au journal (spec 05 §3.2 : la DSM ne porte pas de
 *                   « dernière modification ») ; absente sans entrée
 * @param assiette   le total de la DSM pour chaque colonne de l'assiette (RG-DEC-31), de quoi calculer sa
 *                   cotisation sans la charger
 * @param htFacture  RG-DEC-34 : présent si la DSM a une facture ; ses factures moins ses avoirs
 */
public record DsmLigne(long id, long operateur, TypeDeclarant type, PeriodeDeclaration periode, EtatDsm etat,
        Instant envoyeeLe, Instant valideeLe, Instant modifieeLe, Map<ColonneDsm, VolumeAlcoolPur> assiette,
        Optional<BigDecimal> htFacture) {

    public DsmLigne {
        assiette = Map.copyOf(assiette);
    }

    /** RG-DEC-34. */
    public boolean facturee() {
        return htFacture.isPresent();
    }

    /** Le total d'une colonne, nul hors de l'assiette. */
    public VolumeAlcoolPur total(ColonneDsm colonne) {
        return assiette.getOrDefault(colonne, VolumeAlcoolPur.ZERO);
    }
}
