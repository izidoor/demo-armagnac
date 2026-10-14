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
     * vieillissent : {@code MB} recalculé, {@code NB} +1, bornés à {@code 20+} ; {@code MV} et {@code NV}
     * inchangés. Les lignes devenues identiques fusionnent, volumes additionnés, dans l'ordre de la première.
     */
    public static List<LigneStockInitial> reprise(DeclarationStocksMouvements precedente, LocalDate debut) {
        return reprise(precedente.lignes(), debut);
    }

    /** RG-DEC-14, 38 : de même pour les lignes d'un fichier importé en stock initial. */
    static List<LigneStockInitial> reprise(List<LigneDsm> lignes, LocalDate debut) {
        boolean anniversaire = debut.getMonth() == Month.APRIL && debut.getDayOfMonth() == 1;
        Map<IdentiteLigne, VolumeAlcoolPur> stock = new LinkedHashMap<>();
        for (LigneDsm ligne : lignes) {
            VolumeAlcoolPur fin = ligne.stockFin();
            if (fin.estNegatif() || fin.estNul()) {
                continue;
            }
            IdentiteLigne identite = anniversaire ? vieillie(ligne.identite(), debut) : ligne.identite();
            stock.merge(identite, fin, VolumeAlcoolPur::plus);
        }
        return stock.entrySet().stream().map(e -> new LigneStockInitial(e.getKey(), e.getValue())).toList();
    }

    /** Au 1er avril : seul le fût vieillit (H-DEC-9). */
    static IdentiteLigne vieillie(IdentiteLigne identite, LocalDate debut) {
        CompteDAgeDsm compte = switch (identite.cle()) {
            case MB -> CompteDAgeDsm.calcule(identite.millesime(), debut);
            case NB -> identite.compte().suivant();
            case MV, NV -> identite.compte();
        };
        return new IdentiteLigne(identite.cle(), identite.millesime(), identite.denomination(), compte);
    }
}
