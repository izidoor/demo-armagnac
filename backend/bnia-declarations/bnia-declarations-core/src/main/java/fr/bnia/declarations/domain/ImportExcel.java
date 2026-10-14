package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.ImportRefuse.Erreur;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôle du modèle Excel importé (spec 05 §5.1, H-DEC-15), sans état propre : tout ou rien (RG-DEC-37). Clé
 * inconnue, identité invalide, volume non numérique ou négatif, ligne en double, valeur en colonne grisée,
 * période du fichier différente : chaque erreur est située, et une seule refuse le fichier. Colonnes 1 et 26 du
 * fichier lues pour les avertissements seulement, compte d'une ligne {@code MB} recalculé (RG-DEC-38).
 */
public final class ImportExcel {

    private static final DateTimeFormatter MOIS = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ImportExcel() {
    }

    /**
     * Une ligne du fichier, contrôlée.
     *
     * @param ligne       son numéro de ligne au récapitulatif, pour les avertissements
     * @param mouvements  colonnes 2 à 25, non nulles
     * @param stockDebut  la colonne 1 du fichier, si elle est remplie
     * @param stockFin    la colonne 26 du fichier, si elle est remplie (ou calculée par le tableur)
     */
    public record LigneImportee(int ligne, IdentiteLigne identite, Map<ColonneDsm, VolumeAlcoolPur> mouvements,
            Optional<VolumeAlcoolPur> stockDebut, Optional<VolumeAlcoolPur> stockFin) {

        public LigneImportee {
            mouvements = Map.copyOf(mouvements);
        }

        /** « Ligne 23 du fichier (« millésime 2010, clé MB, compte d'âge 14 ») ». */
        String libelle() {
            return "Ligne " + ligne + " du fichier (" + identite.libelle() + ")";
        }
    }

    /** Les lignes d'un fichier sans erreur, et ses avertissements. */
    public record Lecture(List<LigneImportee> lignes, List<String> avertissements) {

        public Lecture {
            lignes = List.copyOf(lignes);
            avertissements = List.copyOf(avertissements);
        }
    }

    /** Un stock initial importé : début, lignes et avertissements. */
    public record StockImporte(LocalDate debut, List<LigneStockInitial> lignes, List<String> avertissements) {

        public StockImporte {
            lignes = List.copyOf(lignes);
            avertissements = List.copyOf(avertissements);
        }
    }

    /**
     * RG-DEC-37 : le fichier d'une DSM, qui porte sur sa période.
     *
     * @throws ImportRefuse avec toutes les erreurs du fichier
     */
    public static Lecture pourDsm(ClasseurLu classeur, TypeDeclarant type, PeriodeDeclaration periode) {
        List<Erreur> erreurs = new ArrayList<>();
        classeur.mois().ifPresentOrElse(mois -> {
            if (!mois.equals(YearMonth.from(periode.debut()))) {
                erreurs.add(Erreur.sur(classeur.cellulePeriode(), "Le fichier porte sur " + MOIS.format(mois)
                        + ", la DSM sur " + periode.libelle() + " : la période du fichier doit être celle de la DSM."));
            }
        }, () -> erreurs.add(periodeIllisible(classeur)));
        List<String> avertissements = new ArrayList<>();
        List<LigneImportee> lignes = lire(classeur, type, periode.debut(), erreurs, avertissements);
        if (!erreurs.isEmpty()) {
            throw new ImportRefuse(erreurs);
        }
        return new Lecture(lignes, avertissements);
    }

    /**
     * RG-DEC-37, 38 : le stock de fin du fichier devient un stock initial. Le stock de fin de chaque ligne se
     * calcule sur les colonnes 1 à 25 du fichier (RG-DEC-17) ; les lignes finies à 0 ne sont pas reprises, une
     * ligne finie en négatif refuse le fichier. Au 1er avril, les lignes vieillissent (RG-DEC-14).
     *
     * @param debutChoisi le début du point de départ ; par défaut, le lendemain de la période du fichier
     * @throws ImportRefuse avec toutes les erreurs du fichier
     */
    public static StockImporte stockInitial(ClasseurLu classeur, TypeDeclarant type, Optional<LocalDate> debutChoisi) {
        Optional<PeriodeDeclaration> periode = classeur.mois().map(m -> m.atDay(1))
                .filter(d -> PeriodeDeclaration.debutPermis(type, d))
                .map(d -> PeriodeDeclaration.commencant(type, d));
        if (periode.isEmpty()) {
            throw new ImportRefuse(List.of(classeur.mois().isEmpty() ? periodeIllisible(classeur)
                    : Erreur.sur(classeur.cellulePeriode(), "Le fichier porte sur " + MOIS.format(classeur.mois().get())
                            + " : la période d'un producteur commence en avril.")));
        }
        PeriodeDeclaration fichier = periode.get();
        LocalDate lendemain = fichier.fin().plusDays(1);
        LocalDate debut = debutChoisi.orElse(lendemain);
        List<Erreur> erreurs = new ArrayList<>();
        List<String> avertissements = new ArrayList<>();
        if (!debut.equals(lendemain)) {
            avertissements.add("Le fichier porte sur " + fichier.libelle() + " : son stock de fin du "
                    + DATE.format(fichier.fin()) + " devient le stock initial au " + DATE.format(debut) + ".");
        }
        List<LigneDsm> lignes = new ArrayList<>();
        for (LigneImportee importee : lire(classeur, type, fichier.debut(), erreurs, avertissements)) {
            EnumMap<ColonneDsm, VolumeAlcoolPur> volumes = new EnumMap<>(ColonneDsm.class);
            volumes.putAll(importee.mouvements());
            importee.stockDebut().ifPresent(v -> volumes.put(ColonneDsm.C01, v));
            LigneDsm ligne = new LigneDsm(null, importee.identite(), OrigineLigne.AJOUT, volumes);
            if (ligne.stockFin().estNegatif()) {
                erreurs.add(new Erreur(OngletDsm.RECAPITULATIF, importee.ligne(), "I", "Le stock de fin calculé ("
                        + litres(ligne.stockFin()) + ") est négatif : il ne peut pas devenir un stock initial."));
            }
            ecartStockFin(importee, ligne, avertissements);
            lignes.add(ligne);
        }
        if (!erreurs.isEmpty()) {
            throw new ImportRefuse(erreurs);
        }
        return new StockImporte(debut, Vieillissement.reprise(lignes, debut), avertissements);
    }

    /** RG-DEC-38 : la colonne 26 du fichier est ignorée ; un écart avec le calcul est signalé. */
    static void ecartStockFin(LigneImportee importee, LigneDsm ligne, List<String> avertissements) {
        importee.stockFin().filter(lu -> lu.compareTo(ligne.stockFin()) != 0).ifPresent(lu -> avertissements.add(
                importee.libelle() + " : stock de fin " + litres(lu) + " dans le fichier, " + litres(ligne.stockFin())
                        + " calculé ; le calcul prévaut."));
    }

    private static Erreur periodeIllisible(ClasseurLu classeur) {
        return Erreur.sur(classeur.cellulePeriode(),
                "La période du fichier est absente ou illisible : une date du mois déclaré est attendue.");
    }

    private static List<LigneImportee> lire(ClasseurLu classeur, TypeDeclarant type, LocalDate debutPeriode,
            List<Erreur> erreurs, List<String> avertissements) {
        List<LigneImportee> lignes = new ArrayList<>();
        Map<IdentiteLigne, Integer> vues = new HashMap<>();
        for (ClasseurLu.Rangee rangee : classeur.rangees()) {
            if (rangee.vide()) {
                continue;
            }
            int avant = erreurs.size();
            IdentiteLigne identite = identite(rangee, debutPeriode, erreurs, avertissements);
            EnumMap<ColonneDsm, VolumeAlcoolPur> mouvements = new EnumMap<>(ColonneDsm.class);
            VolumeAlcoolPur stockDebut = null;
            VolumeAlcoolPur stockFin = null;
            for (ColonneDsm colonne : ColonneDsm.values()) {
                CelluleLue cellule = rangee.volumes().get(colonne);
                boolean calculee = colonne == ColonneDsm.C01 || colonne == ColonneDsm.C26;
                if (cellule == null || (calculee ? cellule.absente() : cellule.vide())) {
                    continue;
                }
                BigDecimal nombre = cellule.enNombre();
                if (nombre == null) {
                    erreurs.add(Erreur.sur(cellule, "« " + cellule.texte() + " » n'est pas un volume (" + colonne + ")."));
                    continue;
                }
                VolumeAlcoolPur volume = VolumeAlcoolPur.litres(nombre);
                if (colonne == ColonneDsm.C01) {
                    stockDebut = volume;
                } else if (colonne == ColonneDsm.C26) {
                    stockFin = volume;
                } else if (volume.estNegatif()) {
                    erreurs.add(Erreur.sur(cellule, "Volume négatif (" + litres(volume) + ", " + colonne
                            + ") : un volume est positif ou nul."));
                } else if (colonne.griseePour(type)) {
                    erreurs.add(Erreur.sur(cellule, "La " + colonne + " est grisée pour un producteur : elle reste vide."));
                } else if (!volume.estNul()) {
                    mouvements.put(colonne, volume);
                }
            }
            if (identite != null) {
                Integer premiere = vues.putIfAbsent(identite, rangee.ligne());
                if (premiere != null) {
                    erreurs.add(Erreur.sur(rangee.designation(), "La ligne " + identite.libelle()
                            + " figure deux fois (lignes " + premiere + " et " + rangee.ligne() + ") : même clé, même"
                            + " millésime ou dénomination, même compte d'âge."));
                }
            }
            if (erreurs.size() == avant) {
                lignes.add(new LigneImportee(rangee.ligne(), identite, mouvements, Optional.ofNullable(stockDebut),
                        Optional.ofNullable(stockFin)));
            }
        }
        return lignes;
    }

    /** RG-DEC-10 à 12 : l'identité de la rangée, ou {@code null} après avoir noté l'erreur. */
    private static IdentiteLigne identite(ClasseurLu.Rangee rangee, LocalDate debutPeriode, List<Erreur> erreurs,
            List<String> avertissements) {
        CelluleLue celluleCle = rangee.cle();
        if (celluleCle.texte() == null) {
            erreurs.add(Erreur.sur(celluleCle, "La clé de conditionnement manque (MB, MV, NB ou NV)."));
            return null;
        }
        CleConditionnement cle;
        try {
            cle = CleConditionnement.valueOf(celluleCle.texte().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            erreurs.add(Erreur.sur(celluleCle, "Clé « " + celluleCle.texte() + " » inconnue (attendu : MB, MV, NB ou NV)."));
            return null;
        }
        CelluleLue designation = rangee.designation();
        Integer millesime = null;
        String denomination = null;
        if (cle.millesimee() && designation.texte() != null) {
            BigDecimal annee = designation.enNombre();
            if (annee == null || annee.stripTrailingZeros().scale() > 0) {
                erreurs.add(Erreur.sur(designation, "« " + designation.texte() + " » n'est pas un millésime (ligne "
                        + cle + ")."));
                return null;
            }
            millesime = annee.intValue();
        } else if (!cle.millesimee()) {
            denomination = designation.texte();
        }
        String compte = rangee.compte().texte();
        IdentiteLigne identite;
        try {
            identite = IdentiteLigne.saisie(cle, millesime, denomination, compte, debutPeriode);
        } catch (RegleEnfreinte e) {
            boolean surCompte = e.regle().filter("RG-DEC-12"::equals).isPresent();
            erreurs.add(Erreur.sur(surCompte ? rangee.compte() : designation, e.getMessage()));
            return null;
        }
        if (cle.compteCalcule() && compte != null && !compte.equals(identite.compte().toString())) {
            avertissements.add("Ligne " + rangee.ligne() + " du fichier : compte d'âge « " + compte + " » ignoré, "
                    + identite.compte() + " calculé depuis le millésime " + identite.millesime() + " (RG-DEC-12).");
        }
        return identite;
    }

    /** « 1 234,56 L ». */
    static String litres(VolumeAlcoolPur volume) {
        DecimalFormatSymbols symboles = DecimalFormatSymbols.getInstance(Locale.FRANCE);
        symboles.setGroupingSeparator(' ');
        return new DecimalFormat("#,##0.00", symboles).format(volume.litres()) + " L";
    }
}
