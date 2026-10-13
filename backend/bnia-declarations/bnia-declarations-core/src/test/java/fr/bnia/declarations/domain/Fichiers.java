package fr.bnia.declarations.domain;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Un modèle Excel lu, écrit à la main pour les tests : une rangée par ligne, à partir de la ligne 18 du
 * récapitulatif comme dans la matrice négociant. Une valeur {@code String} est une cellule texte, un nombre une
 * cellule numérique ; la colonne d'une cellule de volume s'écrit par son numéro de colonne de DSM.
 */
public final class Fichiers {

    private final YearMonth mois;
    private final List<ClasseurLu.Rangee> rangees = new ArrayList<>();

    private Fichiers(YearMonth mois) {
        this.mois = mois;
    }

    /** Un fichier qui porte sur ce mois ; {@code null} : période absente. */
    public static Fichiers du(YearMonth mois) {
        return new Fichiers(mois);
    }

    /** Une rangée : désignation (millésime ou dénomination), clé, compte d'âge, volumes par numéro de colonne. */
    public Fichiers ligne(Object designation, String cle, Object compte, Map<Integer, Object> volumes) {
        int ligne = 18 + rangees.size();
        EnumMap<ColonneDsm, CelluleLue> cellules = new EnumMap<>(ColonneDsm.class);
        volumes.forEach((numero, valeur) -> {
            ColonneDsm colonne = ColonneDsm.numero(numero);
            cellules.put(colonne, cellule(colonne.onglet(), ligne, String.valueOf(numero), valeur));
        });
        rangees.add(new ClasseurLu.Rangee(ligne, cellule(OngletDsm.RECAPITULATIF, ligne, "A", designation),
                cellule(OngletDsm.RECAPITULATIF, ligne, "B", cle), cellule(OngletDsm.RECAPITULATIF, ligne, "C", compte),
                cellules));
        return this;
    }

    /** Une rangée vierge, comme celles qui suivent les lignes dans le modèle. */
    public Fichiers vierge() {
        return ligne(null, null, null, Map.of(26, 0));
    }

    public ClasseurLu classeur() {
        return new ClasseurLu(new CelluleLue(OngletDsm.RECAPITULATIF, 8, "A", mois == null ? null : mois.toString(), null),
                Optional.ofNullable(mois), rangees);
    }

    private static CelluleLue cellule(OngletDsm onglet, int ligne, String colonne, Object valeur) {
        return switch (valeur) {
            case null -> new CelluleLue(onglet, ligne, colonne, null, null);
            case Number n -> {
                BigDecimal nombre = new BigDecimal(n.toString());
                yield new CelluleLue(onglet, ligne, colonne, nombre.toPlainString(), nombre);
            }
            default -> new CelluleLue(onglet, ligne, colonne, valeur.toString(), null);
        };
    }
}
