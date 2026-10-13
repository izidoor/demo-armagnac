package fr.bnia.declarations.domain;

import static fr.bnia.declarations.domain.OngletDsm.MOUVEMENTS_ENTREE;
import static fr.bnia.declarations.domain.OngletDsm.MOUVEMENTS_SORTIE;
import static fr.bnia.declarations.domain.OngletDsm.RECAPITULATIF;
import static fr.bnia.declarations.domain.OngletDsm.VENTES_EXPORT;
import static fr.bnia.declarations.domain.OngletDsm.VENTES_FRANCE;
import static fr.bnia.declarations.domain.SensColonne.ENTREE;
import static fr.bnia.declarations.domain.SensColonne.SORTIE;
import static fr.bnia.declarations.domain.SensColonne.STOCK_DEBUT;
import static fr.bnia.declarations.domain.SensColonne.STOCK_FIN;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.util.Arrays;
import java.util.List;

/**
 * Les 26 colonnes du format unique de la DSM (spec 05 §3.1) : référentiel fixe, défini par l'application.
 * Porte ce que les règles en tirent : onglet, sens dans le calcul du stock (RG-DEC-17), grisage pour un
 * producteur (RG-DEC-16). L'assiette, la ligne de cotisation et le justificatif attendu viennent avec
 * les lots 3 et 4.
 */
public enum ColonneDsm {

    C01(1, RECAPITULATIF, "Stock début de période", STOCK_DEBUT, false),
    C02(2, RECAPITULATIF, "Entrées achats à la propriété", ENTREE, true),
    C03(3, RECAPITULATIF, "Entrées achats négoce", ENTREE, true),
    C04(4, MOUVEMENTS_ENTREE, "Distillation propre", ENTREE, false),
    C05(5, MOUVEMENTS_ENTREE, "Passage sous bois / sous-verre", ENTREE, true),
    C06(6, MOUVEMENTS_ENTREE, "Assemblages / coupes", ENTREE, false),
    C07(7, MOUVEMENTS_ENTREE, "Transferts chai Armagnac en région délimitée", ENTREE, false),
    C08(8, MOUVEMENTS_ENTREE, "Transferts chai Armagnac hors région délimitée", ENTREE, false),
    C09(9, MOUVEMENTS_ENTREE, "Réintégrations", ENTREE, true),
    C10(10, MOUVEMENTS_SORTIE, "Pertes sur inventaire", SORTIE, false),
    C11(11, MOUVEMENTS_SORTIE, "Passage sous bois / sous-verre", SORTIE, true),
    C12(12, MOUVEMENTS_SORTIE, "Assemblages / coupes", SORTIE, false),
    C13(13, MOUVEMENTS_SORTIE, "Transferts chai Armagnac en région délimitée", SORTIE, false),
    C14(14, MOUVEMENTS_SORTIE, "Transferts chai Armagnac hors région délimitée", SORTIE, false),
    C15(15, MOUVEMENTS_SORTIE, "Fabrications Floc de Gascogne", SORTIE, false),
    C16(16, MOUVEMENTS_SORTIE, "Utilisation de l'Armagnac pour autres fabrications que l'AOC", SORTIE, false),
    C17(17, VENTES_FRANCE, "Marché intérieur hors chai en bouteilles", SORTIE, false),
    C18(18, VENTES_FRANCE, "Marché intérieur hors chai en vrac destiné à la mise en bouteille", SORTIE, false),
    C19(19, VENTES_FRANCE, "Marché intérieur hors chai en brandies", SORTIE, true),
    C20(20, VENTES_FRANCE, "Négoce chai Armagnac en région délimitée", SORTIE, false),
    C21(21, VENTES_FRANCE, "Négoce chai Armagnac hors région délimitée", SORTIE, false),
    C22(22, VENTES_EXPORT, "Bouteilles", SORTIE, false),
    C23(23, VENTES_EXPORT, "Vrac destiné à la mise en bouteille", SORTIE, false),
    C24(24, VENTES_EXPORT, "Brandies", SORTIE, true),
    C25(25, VENTES_EXPORT, "Vins vinés et fabrications autres", SORTIE, true),
    C26(26, RECAPITULATIF, "Stock fin de période", STOCK_FIN, false);

    private static final List<ColonneDsm> MOUVEMENTS = Arrays.stream(values())
            .filter(c -> c.sens == ENTREE || c.sens == SORTIE).toList();

    private final int numero;
    private final OngletDsm onglet;
    private final String libelle;
    private final SensColonne sens;
    private final boolean griseeProducteur;

    ColonneDsm(int numero, OngletDsm onglet, String libelle, SensColonne sens, boolean griseeProducteur) {
        this.numero = numero;
        this.onglet = onglet;
        this.libelle = libelle;
        this.sens = sens;
        this.griseeProducteur = griseeProducteur;
    }

    public int numero() {
        return numero;
    }

    public OngletDsm onglet() {
        return onglet;
    }

    public String libelle() {
        return libelle;
    }

    public SensColonne sens() {
        return sens;
    }

    /** RG-DEC-16 : colonnes 2, 3, 5, 9, 11, 19, 24 et 25. */
    public boolean griseeProducteur() {
        return griseeProducteur;
    }

    /** RG-DEC-16 : grisée pour ce type de déclarant, donc vide et non saisissable. */
    public boolean griseePour(TypeDeclarant type) {
        return griseeProducteur && type == TypeDeclarant.PRODUCTEUR;
    }

    /** Colonnes 2 à 25 : les mouvements, saisis par l'opérateur (RG-DEC-17). */
    public static List<ColonneDsm> mouvements() {
        return MOUVEMENTS;
    }

    /** RG-DEC-15, 16 : la colonne se saisit pour ce type de déclarant. */
    public boolean saisissablePour(TypeDeclarant type) {
        return (sens == ENTREE || sens == SORTIE) && !griseePour(type);
    }

    public static ColonneDsm numero(int numero) {
        if (numero < 1 || numero > 26) {
            throw new RegleEnfreinte(null, "Colonne de DSM inconnue : " + numero + " (attendu : 1 à 26).");
        }
        return values()[numero - 1];
    }

    @Override
    public String toString() {
        return "colonne " + numero;
    }
}
