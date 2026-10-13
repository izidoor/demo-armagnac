package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Ligne d'une DSM (entité, spec 05 §3.2.1) : identité, origine et volumes des colonnes 1 à 25, en litres
 * d'alcool pur. La colonne 26, stock de fin, est calculée (RG-DEC-17). Immuable : la DSM remplace ses
 * lignes à chaque enregistrement.
 *
 * @param id identifiant technique, {@code null} avant le premier enregistrement
 */
public record LigneDsm(Long id, IdentiteLigne identite, OrigineLigne origine, Map<ColonneDsm, VolumeAlcoolPur> volumes) {

    public LigneDsm {
        Objects.requireNonNull(identite, "identite");
        Objects.requireNonNull(origine, "origine");
        EnumMap<ColonneDsm, VolumeAlcoolPur> copie = new EnumMap<>(ColonneDsm.class);
        volumes.forEach((colonne, volume) -> {
            if (colonne != ColonneDsm.C26 && volume != null && !volume.estNul()) {
                copie.put(colonne, volume);
            }
        });
        volumes = Collections.unmodifiableMap(copie);
    }

    /** RG-DEC-13 : ligne reprise du stock initial ou de la DSM précédente, colonne 1 = ce stock. */
    static LigneDsm reprise(IdentiteLigne identite, VolumeAlcoolPur stock) {
        return new LigneDsm(null, identite, OrigineLigne.REPRISE, Map.of(ColonneDsm.C01, stock));
    }

    /** Le volume d'une colonne, 0 si rien n'est saisi ; la colonne 26 est calculée. */
    public VolumeAlcoolPur volume(ColonneDsm colonne) {
        if (colonne == ColonneDsm.C26) {
            return stockFin();
        }
        return volumes.getOrDefault(colonne, VolumeAlcoolPur.ZERO);
    }

    public VolumeAlcoolPur stockDebut() {
        return volume(ColonneDsm.C01);
    }

    /** RG-DEC-17 : colonnes 2 à 9. */
    public VolumeAlcoolPur totalEntrees() {
        return somme(SensColonne.ENTREE);
    }

    /** RG-DEC-17 : colonnes 10 à 25. */
    public VolumeAlcoolPur totalSorties() {
        return somme(SensColonne.SORTIE);
    }

    /** RG-DEC-17 : 26 = 1 + 2 + 3 + Σ(4 à 9) − Σ(10 à 25). */
    public VolumeAlcoolPur stockFin() {
        return stockDebut().plus(totalEntrees()).moins(totalSorties());
    }

    private VolumeAlcoolPur somme(SensColonne sens) {
        return volumes.entrySet().stream()
                .filter(e -> e.getKey().sens() == sens)
                .map(Map.Entry::getValue)
                .reduce(VolumeAlcoolPur.ZERO, VolumeAlcoolPur::plus);
    }

    LigneDsm avecId(Long nouvelId) {
        return new LigneDsm(nouvelId, identite, origine, volumes);
    }

    /** RG-DEC-08 : la colonne 1 et l'origine suivent le stock initial ; les mouvements restent. */
    LigneDsm avecStockDebut(OrigineLigne nouvelleOrigine, VolumeAlcoolPur stock) {
        EnumMap<ColonneDsm, VolumeAlcoolPur> copie = new EnumMap<>(ColonneDsm.class);
        copie.putAll(volumes);
        copie.put(ColonneDsm.C01, stock);
        return new LigneDsm(id, identite, nouvelleOrigine, copie);
    }
}
