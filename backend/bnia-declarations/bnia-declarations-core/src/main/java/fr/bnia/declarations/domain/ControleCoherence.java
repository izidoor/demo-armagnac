package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.partage.CompteDAgeDsm;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Contrôles de cohérence d'une DSM (glossaire, RG-DEC-18, H-DEC-7, H-DEC-9) : anomalies de calcul, sans
 * état propre. Les pièces manquantes viennent avec le lot 3 (RG-DEC-29).
 */
public final class ControleCoherence {

    private ControleCoherence() {
    }

    /**
     * RG-DEC-18 : un stock de fin négatif sur une ligne ; Σ6 ≠ Σ12 ; pour un négociant, Σ5 ≠ Σ11 (égalités
     * exactes au centilitre) ; un compte {@code MV} supérieur au compte calculé de son millésime.
     */
    public static List<Anomalie> anomalies(DeclarationStocksMouvements dsm) {
        List<Anomalie> anomalies = new ArrayList<>();
        List<LigneDsm> lignes = dsm.lignes();
        for (int rang = 0; rang < lignes.size(); rang++) {
            LigneDsm ligne = lignes.get(rang);
            IdentiteLigne identite = ligne.identite();
            if (identite.compteTropVieux(dsm.periode().debut())) {
                anomalies.add(new Anomalie(TypeAnomalie.COMPTE_MV_TROP_VIEUX, NiveauAnomalie.LIGNE,
                        OngletDsm.RECAPITULATIF, null, rang, "Ligne " + identite.libelle() + " : le compte d'âge dépasse "
                        + CompteDAgeDsm.calcule(identite.millesime(), dsm.periode().debut())
                        + ", celui du millésime " + identite.millesime() + " au début de la période.", null));
            }
            VolumeAlcoolPur stockFin = ligne.stockFin();
            if (stockFin.estNegatif()) {
                anomalies.add(new Anomalie(TypeAnomalie.STOCK_FIN_NEGATIF, NiveauAnomalie.LIGNE,
                        OngletDsm.RECAPITULATIF, ColonneDsm.C26, rang, "Ligne " + identite.libelle()
                        + " : le stock fin de période (colonne 26) est négatif, " + litres(stockFin) + ".", stockFin));
            }
        }
        croisees(dsm, ColonneDsm.C06, ColonneDsm.C12, TypeAnomalie.ASSEMBLAGES_DESEQUILIBRES, "assemblages / coupes")
                .ifPresent(anomalies::add);
        if (dsm.type() == TypeDeclarant.NEGOCIANT) {
            croisees(dsm, ColonneDsm.C05, ColonneDsm.C11, TypeAnomalie.PASSAGES_SOUS_BOIS_DESEQUILIBRES,
                    "passages sous bois / sous-verre").ifPresent(anomalies::add);
        }
        return List.copyOf(anomalies);
    }

    /** H-DEC-7 : sur les totaux de la DSM, l'entrée et la sortie croisées sont égales au centilitre. */
    private static Optional<Anomalie> croisees(DeclarationStocksMouvements dsm, ColonneDsm entree, ColonneDsm sortie,
            TypeAnomalie type, String libelle) {
        VolumeAlcoolPur ecart = dsm.total(entree).moins(dsm.total(sortie));
        if (ecart.estNul()) {
            return Optional.empty();
        }
        return Optional.of(new Anomalie(type, NiveauAnomalie.ONGLET, entree.onglet(), entree, null,
                "Les " + libelle + " en entrée (colonne " + entree.numero() + ", " + litres(dsm.total(entree))
                        + ") et en sortie (colonne " + sortie.numero() + ", " + litres(dsm.total(sortie))
                        + ") diffèrent de " + litres(ecart) + ".", ecart));
    }

    /** Un volume pour l'écran, à la virgule : « -81,40 L ». */
    private static String litres(VolumeAlcoolPur volume) {
        return volume.toString().replace('.', ',') + " L";
    }
}
