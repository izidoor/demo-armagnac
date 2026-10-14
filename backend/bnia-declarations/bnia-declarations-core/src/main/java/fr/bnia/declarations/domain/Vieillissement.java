package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.partage.CompteDAgeDsm;
import java.time.LocalDate;
import java.time.Month;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Vieillissement du stock d'une DSM à l'autre (spec 05 §3.6, RG-DEC-14, H-DEC-9, H-DEC-11), sans état
 * propre.
 */
public final class Vieillissement {

    private Vieillissement() {
    }

    /**
     * RG-DEC-14 : le stock de départ de la DSM qui suit {@code precedente}. Chaque ligne dont le stock de fin est
     * strictement positif est reprise, colonne 1 = ce stock. Si la période commence un 1er avril, les lignes
     * vieillissent : compte {@code MB} recalculé depuis son millésime, {@code NB} +1, bornés à {@code 20+} ;
     * {@code MV} et {@code NV} inchangés. Les lignes devenues identiques fusionnent, volumes additionnés, dans
     * l'ordre de la première.
     */
    public static List<LigneStockInitial> reprise(DeclarationStocksMouvements precedente, LocalDate debut) {
        return reprise(precedente.lignes(), debut, debut);
    }

    /**
     * RG-DEC-09, 14 : le stock proposé pour un nouveau point de départ, au {@code debut}, après la DSM
     * {@code derniere} : de même, mais une ligne {@code NB} prend +1 à chaque 1er avril franchi depuis la fin de
     * sa période (décision du 2026-10-14) ; le compte {@code MB} est recalculé depuis son millésime à la nouvelle
     * date.
     */
    public static List<LigneStockInitial> apresInterruption(DeclarationStocksMouvements derniere, LocalDate debut) {
        return reprise(derniere.lignes(), derniere.periode().fin().plusDays(1), debut);
    }

    /** RG-DEC-14, 38 : de même pour les lignes d'un fichier importé en stock initial. */
    static List<LigneStockInitial> reprise(List<LigneDsm> lignes, LocalDate debut) {
        return reprise(lignes, debut, debut);
    }

    /** @param depuis le lendemain de la fin du stock repris : les 1er avril de {@code depuis} à {@code debut} comptent */
    private static List<LigneStockInitial> reprise(List<LigneDsm> lignes, LocalDate depuis, LocalDate debut) {
        int anniversaires = anniversaires(depuis, debut);
        Map<IdentiteLigne, VolumeAlcoolPur> stock = new LinkedHashMap<>();
        for (LigneDsm ligne : lignes) {
            VolumeAlcoolPur fin = ligne.stockFin();
            if (fin.estNegatif() || fin.estNul()) {
                continue;
            }
            IdentiteLigne identite = anniversaires > 0 ? vieillie(ligne.identite(), debut, anniversaires)
                    : ligne.identite();
            stock.merge(identite, fin, VolumeAlcoolPur::plus);
        }
        return stock.entrySet().stream().map(e -> new LigneStockInitial(e.getKey(), e.getValue())).toList();
    }

    /** Le nombre de 1er avril de {@code depuis} à {@code debut}, bornes comprises. */
    static int anniversaires(LocalDate depuis, LocalDate debut) {
        int n = 0;
        for (int annee = depuis.getYear(); annee <= debut.getYear(); annee++) {
            LocalDate avril = LocalDate.of(annee, Month.APRIL, 1);
            if (!avril.isBefore(depuis) && !avril.isAfter(debut)) {
                n++;
            }
        }
        return n;
    }

    /**
     * Seul le fût vieillit (H-DEC-9) : compte {@code MB} recalculé depuis son millésime au début, {@code NB} +1 par
     * 1er avril.
     */
    static IdentiteLigne vieillie(IdentiteLigne identite, LocalDate debut, int anniversaires) {
        CompteDAgeDsm compte = switch (identite.cle()) {
            case MB -> CompteDAgeDsm.calcule(identite.millesime(), debut);
            case NB -> {
                CompteDAgeDsm c = identite.compte();
                for (int i = 0; i < anniversaires; i++) {
                    c = c.suivant();
                }
                yield c;
            }
            case MV, NV -> identite.compte();
        };
        return new IdentiteLigne(identite.cle(), identite.millesime(), identite.denomination(), compte);
    }
}
