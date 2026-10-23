package fr.bnia.contratsarmagnac.domain;

import fr.bnia.declarations.domain.partage.CleConditionnement;
import fr.bnia.declarations.domain.partage.CompteDAgeDsm;
import fr.bnia.declarations.domain.partage.DenominationNormalisee;
import java.util.Objects;
import java.util.Optional;

/**
 * Le grain d'une ligne, comparé au stock estimé (RG-CA-05) : toujours sous bois, {@code MB} et son millésime (le compte
 * s'en déduit à toute date), ou {@code NB}, sa dénomination normalisée (RG-DEC-11) et son compte d'âge borné à
 * {@code 20+} (RG-DEC-12). L'appellation n'y entre pas.
 */
public record Grain(CleConditionnement cle, Integer millesime, String denomination, CompteDAgeDsm compte) {

    public Grain {
        Objects.requireNonNull(cle, "cle");
    }

    public static Grain millesime(int millesime) {
        return new Grain(CleConditionnement.MB, millesime, null, null);
    }

    public static Grain assemblage(String denomination, CompteDAgeDsm compte) {
        return new Grain(CleConditionnement.NB, null, DenominationNormalisee.de(denomination), compte);
    }

    /**
     * Le grain d'une ligne de stock ; vide pour une ligne sous verre ({@code MV}, {@code NV}), qu'un contrat ne touche
     * pas.
     */
    public static Optional<Grain> duStock(CleConditionnement cle, Integer millesime, String denomination,
            CompteDAgeDsm compte) {
        return switch (cle) {
            case MB -> Optional.of(millesime(millesime));
            case NB -> Optional.of(assemblage(denomination, compte));
            case MV, NV -> Optional.empty();
        };
    }
}
