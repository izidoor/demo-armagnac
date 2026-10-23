package fr.bnia.declarations.adapter.document;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.declarations.application.ClasseursDsm;
import fr.bnia.declarations.application.CotisationDsm.Calculee;
import fr.bnia.declarations.application.CotisationDsm.Facturee;
import fr.bnia.declarations.application.CotisationDsm;
import fr.bnia.declarations.application.EditionsDsm.EditionDsm;
import fr.bnia.declarations.application.FactureDsm;
import fr.bnia.declarations.domain.CelluleLue;
import fr.bnia.declarations.domain.ClasseurLu;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.GrilleCotisation.TauxEnVigueur;
import fr.bnia.declarations.domain.IdentiteLigne;
import fr.bnia.declarations.domain.LigneDsm;
import fr.bnia.declarations.domain.OngletDsm;
import fr.bnia.declarations.domain.PolitiqueCotisation.Detail;
import fr.bnia.declarations.domain.PolitiqueCotisation.LigneDetail;
import fr.bnia.declarations.domain.TypeDeclarant;
import fr.bnia.declarations.domain.partage.EtatDsm;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.dhatim.fastexcel.PaperSize;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.springframework.stereotype.Component;

/**
 * Le modèle Excel de la DSM (RG-DEC-36 à 38, ADR-020, H-DEC-15, H-DEC-21) : la matrice négociant du BNIA,
 * reconstruite case pour case. Cinq onglets aux noms du modèle, lignes déclarées au récapitulatif (les autres
 * onglets les reprennent par formule, rangée pour rangée), colonnes numérotées, cellules en litres au masque
 * {@code #0\.00.00} (hl.l,cl), totaux et colonne 26 en formules, ligne « base cotisation », colonnes grisées
 * pour un producteur ; un 6e onglet « COTISATION », ignoré à l'import. Le modèle vierge du BNIA se lit aussi :
 * la lecture repère les numéros de colonne, et la période en A8 du récapitulatif.
 */
@Component
class ExcelDsm implements ClasseursDsm {

    private static final String FORMAT_VOLUME = "#0\\.00.00";
    private static final String FORMAT_EUROS = "#,##0.00";
    private static final String GRIS = "BFBFBF";
    private static final String ENTETE = "DDEBF7";
    private static final int RANGEES_MINIMUM = 250;
    /** Ligne de la période au récapitulatif (A8), comme dans le modèle. */
    private static final int LIGNE_PERIODE = 8;
    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final List<DateTimeFormatter> MOIS_LUS = List.of(DateTimeFormatter.ofPattern("MM/yyyy"),
            DateTimeFormatter.ofPattern("M/yyyy"), DateTimeFormatter.ofPattern("yyyy-MM"),
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH), DateTimeFormatter.ofPattern("MMM-yy", Locale.FRENCH));

    /**
     * Un onglet du modèle : son nom, son titre, la ligne de ses numéros de colonne (à partir de 1) ; ses colonnes
     * commencent en D, après millésime ou dénomination, clé et compte d'âge.
     */
    private enum Feuille {
        RECAPITULATIF(OngletDsm.RECAPITULATIF, "RECAPITULATIF", "RECAPITULATIF DES STOCKS ET MOUVEMENTS", 17),
        ENTREES(OngletDsm.MOUVEMENTS_ENTREE, "MOUVEMENTS ENTREE", "MOUVEMENTS EN ENTREE", 8),
        SORTIES(OngletDsm.MOUVEMENTS_SORTIE, "MVMTS SORTIES & FABRICATIONS", "MOUVEMENTS EN SORTIE ET FABRICATIONS", 8),
        FRANCE(OngletDsm.VENTES_FRANCE, "VENTES FRANCE", "VENTES EN FRANCE", 8),
        EXPORT(OngletDsm.VENTES_EXPORT, "VENTES EXPORT & VINS VINES", "VENTES A L'EXPORTATION ET VINS VINES", 7);

        final OngletDsm onglet;
        final String nom;
        final String titre;
        final int ligneNumeros;

        Feuille(OngletDsm onglet, String nom, String titre, int ligneNumeros) {
            this.onglet = onglet;
            this.nom = nom;
            this.titre = titre;
            this.ligneNumeros = ligneNumeros;
        }

        /** La première ligne de données, à partir de 1. */
        int premiere() {
            return ligneNumeros + 1;
        }

        /** Les colonnes de DSM de l'onglet, avec leur index de colonne (à partir de 0). */
        Map<ColonneDsm, Integer> colonnes() {
            Map<ColonneDsm, Integer> colonnes = new LinkedHashMap<>();
            if (this == RECAPITULATIF) {
                colonnes.put(ColonneDsm.C01, 3);
                colonnes.put(ColonneDsm.C02, 4);
                colonnes.put(ColonneDsm.C03, 5);
                colonnes.put(ColonneDsm.C26, 8);
                return colonnes;
            }
            int index = 3;
            for (ColonneDsm c : ColonneDsm.values()) {
                if (c.onglet() == onglet) {
                    colonnes.put(c, index++);
                }
            }
            return colonnes;
        }

        /** La référence d'une cellule de cet onglet, depuis un autre : {@code 'MOUVEMENTS ENTREE'!D9}. */
        String ref(int index, int ligne) {
            return "'" + nom + "'!" + lettre(index) + ligne;
        }
    }

    // --- Écriture (RG-DEC-36) ---

    @Override
    public byte[] ecrire(EditionDsm edition) {
        DeclarationStocksMouvements dsm = edition.dsm();
        int rangees = Math.max(RANGEES_MINIMUM, dsm.lignes().size());
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        try {
            Workbook classeur = new Workbook(sortie, "BNIA", "1.0");
            for (Feuille feuille : Feuille.values()) {
                Worksheet ws = classeur.newWorksheet(feuille.nom);
                if (feuille == Feuille.RECAPITULATIF) {
                    enteteRecapitulatif(ws, edition);
                } else {
                    entete(ws, edition, feuille);
                }
                colonnes(ws, feuille, dsm);
                lignes(ws, feuille, dsm, rangees);
                pied(ws, feuille, dsm.type(), rangees);
            }
            cotisation(classeur.newWorksheet("COTISATION"), edition);
            classeur.finish();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return sortie.toByteArray();
    }

    private static void enteteRecapitulatif(Worksheet ws, EditionDsm edition) {
        DeclarationStocksMouvements dsm = edition.dsm();
        ws.value(0, 0, "BUREAU NATIONAL INTERPROFESSIONNEL DE L'ARMAGNAC");
        ws.style(0, 0).bold().set();
        ws.value(0, 5, "DÉCLARATION DE STOCKS ET DE MOUVEMENTS");
        ws.style(0, 5).bold().fontSize(14).set();
        provisoire(ws, dsm, 2, 0);
        String[][] champs = {
                {"N° ressortissant", edition.operateur().code()},
                {"Nom ou raison sociale", edition.operateur().raisonSociale()},
                {"Type de déclarant", dsm.type().libelle()},
                null,
                {"État", dsm.etat().libelle(edition.cote())},
                {"Validée le", dsm.valideeLe() == null ? "—" : dsm.valideeLe().atZone(PARIS).toLocalDate().format(DATE)
                        + (dsm.valideePar() == null ? "" : " par " + dsm.valideePar().nom())},
                {"Date de DIA", dsm.dateDia() == null ? "—" : dsm.dateDia().format(DATE)},
                {"Échéance", dsm.periode().echeance(dsm.type()).format(DATE)}};
        for (int i = 0; i < champs.length; i++) {
            if (champs[i] != null) {
                ws.value(4 + i, 0, champs[i][0]);
                ws.style(4 + i, 0).bold().set();
                ws.value(4 + i, 2, champs[i][1]);
            }
        }
        // A8 : la période, une date du mois déclaré, relue à l'import (RG-DEC-37).
        ws.value(LIGNE_PERIODE - 1, 0, dsm.periode().debut());
        ws.style(LIGNE_PERIODE - 1, 0).format("mmmm yyyy").bold().horizontalAlignment("left").set();
        ws.value(LIGNE_PERIODE - 1, 2, "Période du " + dsm.periode().debut().format(DATE) + " au "
                + dsm.periode().fin().format(DATE));
        ws.value(13, 0, Feuille.RECAPITULATIF.titre);
        ws.style(13, 0).bold().set();
        ws.value(13, 8, "en Hl.l,cl AP");
        ws.style(13, 8).italic().horizontalAlignment("right").set();
    }

    private static void entete(Worksheet ws, EditionDsm edition, Feuille feuille) {
        DeclarationStocksMouvements dsm = edition.dsm();
        provisoire(ws, dsm, 0, 0);
        String[][] champs = {{"N° ressortissant", edition.operateur().code()},
                {"Nom ou raison sociale", edition.operateur().raisonSociale()},
                {"Période", dsm.periode().libelle()}};
        for (int i = 0; i < champs.length; i++) {
            ws.value(i, 4, champs[i][0]);
            ws.style(i, 4).bold().set();
            ws.value(i, 5, champs[i][1]);
        }
        int derniere = feuille == Feuille.ENTREES ? 11 : 3 + feuille.colonnes().size() - 1;
        ws.value(2, derniere, "Page " + (feuille.ordinal() + 1) + "/5");
        ws.value(4, 0, feuille.titre);
        ws.style(4, 0).bold().set();
    }

    private static void provisoire(Worksheet ws, DeclarationStocksMouvements dsm, int ligne, int colonne) {
        if (dsm.etat() != EtatDsm.VALIDEE) {
            ws.value(ligne, colonne, "Document provisoire, DSM non validée");
            ws.style(ligne, colonne).bold().fontColor("C00000").set();
        }
    }

    /** Les intitulés : identité, libellés et numéros des colonnes, grisées pour un producteur. */
    private static void colonnes(Worksheet ws, Feuille feuille, DeclarationStocksMouvements dsm) {
        int numeros = feuille.ligneNumeros - 1;
        int libelles = numeros - 1;
        String[] identite = {"Millésime ou Dénomination", "Clé", "Cpte Age"};
        for (int c = 0; c < identite.length; c++) {
            ws.value(libelles, c, identite[c]);
            styleEntete(ws, libelles, c, false);
            styleEntete(ws, numeros, c, false);
        }
        ws.width(0, 26);
        ws.width(1, 6);
        ws.width(2, 8);
        feuille.colonnes().forEach((colonne, index) -> {
            boolean grisee = colonne.griseePour(dsm.type());
            ws.value(libelles, index, colonne == ColonneDsm.C10 ? "Pertes sur inventaire DIA du "
                    + (dsm.dateDia() == null ? "../../.." : dsm.dateDia().format(DATE)) + "*" : colonne.libelle());
            styleEntete(ws, libelles, index, grisee);
            ws.value(numeros, index, colonne.numero());
            styleEntete(ws, numeros, index, grisee);
            ws.width(index, 15);
        });
        if (feuille == Feuille.RECAPITULATIF) {
            ws.value(libelles, 6, "Total mouvements entrées (calculé)");
            ws.value(libelles, 7, "Total mouvements et ventes (calculé)");
            for (int index = 6; index <= 7; index++) {
                styleEntete(ws, libelles, index, false);
                styleEntete(ws, numeros, index, false);
                ws.width(index, 15);
            }
        }
        if (feuille == Feuille.ENTREES) {
            // Les colonnes croisées 12 et 11, reprises de l'onglet des sorties (RG-DEC-18), à côté de 6 et 5.
            ws.value(libelles, 10, ColonneDsm.C12.libelle() + " (sorties)");
            ws.value(libelles, 11, ColonneDsm.C11.libelle() + " (sorties)");
            ws.value(numeros, 10, 12);
            ws.value(numeros, 11, 11);
            for (int index = 10; index <= 11; index++) {
                boolean grisee = (index == 11) && ColonneDsm.C11.griseePour(dsm.type());
                styleEntete(ws, libelles, index, grisee);
                styleEntete(ws, numeros, index, grisee);
                ws.width(index, 15);
                // Masquées, comme dans la matrice du BNIA : des copies pour le contrôle, pas des saisies.
                ws.hideColumn(index);
            }
        }
        ws.freezePane(3, feuille.ligneNumeros);
        // Impression : A4 paysage, une page en largeur, intitulés répétés.
        ws.pageOrientation("landscape");
        ws.paperSize(PaperSize.A4_PAPER);
        ws.setFitToPage(true);
        ws.fitToWidth((short) 1);
        ws.fitToHeight((short) 0);
        ws.repeatRows(libelles, numeros);
    }

    private static void styleEntete(Worksheet ws, int ligne, int colonne, boolean grisee) {
        ws.style(ligne, colonne).bold().wrapText(true).horizontalAlignment("center").verticalAlignment("center")
                .fillColor(grisee ? GRIS : ENTETE).borderStyle("thin").set();
    }

    /**
     * Une rangée par ligne de la DSM, par compte d'âge croissant (RG-DEC-45), puis des rangées vierges : le classeur
     * d'un brouillon sert de modèle. L'import rapproche les lignes par leur identité, pas par leur rang.
     */
    private static void lignes(Worksheet ws, Feuille feuille, DeclarationStocksMouvements dsm, int rangees) {
        Map<ColonneDsm, Integer> colonnes = feuille.colonnes();
        List<LigneDsm> ordonnees = dsm.lignesParCompte();
        for (int i = 0; i < rangees; i++) {
            int ligne = feuille.premiere() + i;
            int recap = Feuille.RECAPITULATIF.premiere() + i;
            LigneDsm ligneDsm = i < ordonnees.size() ? ordonnees.get(i) : null;
            if (feuille == Feuille.RECAPITULATIF) {
                identite(ws, ligne, ligneDsm);
            } else {
                for (int c = 0; c < 3; c++) {
                    String ref = "RECAPITULATIF!" + lettre(c) + recap;
                    formule(ws, ligne - 1, c, "IF(ISBLANK(" + ref + "),\"\"," + ref + ")");
                    ws.style(ligne - 1, c).borderStyle("thin").set();
                }
            }
            for (Map.Entry<ColonneDsm, Integer> e : colonnes.entrySet()) {
                ColonneDsm colonne = e.getKey();
                int index = e.getValue();
                boolean grisee = colonne.griseePour(dsm.type());
                if (colonne == ColonneDsm.C26) {
                    formule(ws, ligne - 1, index, "D" + ligne + "+E" + ligne + "+F" + ligne + "+G" + ligne + "-H" + ligne);
                } else if (!grisee && ligneDsm != null && !ligneDsm.volume(colonne).estNul()) {
                    ws.value(ligne - 1, index, ligneDsm.volume(colonne).litres());
                }
                styleVolume(ws, ligne - 1, index, grisee);
            }
            if (feuille == Feuille.RECAPITULATIF) {
                formule(ws, ligne - 1, 6, somme(Feuille.ENTREES, i));
                formule(ws, ligne - 1, 7, somme(Feuille.SORTIES, i) + "+" + somme(Feuille.FRANCE, i) + "+"
                        + somme(Feuille.EXPORT, i));
                styleVolume(ws, ligne - 1, 6, false);
                styleVolume(ws, ligne - 1, 7, false);
            }
            if (feuille == Feuille.ENTREES) {
                Map<ColonneDsm, Integer> sorties = Feuille.SORTIES.colonnes();
                formule(ws, ligne - 1, 10, Feuille.SORTIES.ref(sorties.get(ColonneDsm.C12), Feuille.SORTIES.premiere() + i));
                formule(ws, ligne - 1, 11, Feuille.SORTIES.ref(sorties.get(ColonneDsm.C11), Feuille.SORTIES.premiere() + i));
                styleVolume(ws, ligne - 1, 10, false);
                styleVolume(ws, ligne - 1, 11, ColonneDsm.C11.griseePour(dsm.type()));
            }
        }
    }

    private static void identite(Worksheet ws, int ligne, LigneDsm ligneDsm) {
        if (ligneDsm != null) {
            IdentiteLigne identite = ligneDsm.identite();
            if (identite.millesime() != null) {
                ws.value(ligne - 1, 0, identite.millesime().annee());
            } else {
                ws.value(ligne - 1, 0, identite.denomination().texte());
            }
            ws.value(ligne - 1, 1, identite.cle().name());
            ws.value(ligne - 1, 2, identite.compte().toString());
        }
        for (int c = 0; c < 3; c++) {
            // Texte : « 00 » resterait sinon un nombre, 0.
            ws.style(ligne - 1, c).format("@").borderStyle("thin").horizontalAlignment(c == 0 ? "left" : "center").set();
        }
    }

    /** La somme des colonnes d'un onglet pour une rangée, vue du récapitulatif. */
    private static String somme(Feuille feuille, int rang) {
        return feuille.colonnes().values().stream().map(index -> feuille.ref(index, feuille.premiere() + rang))
                .collect(Collectors.joining("+"));
    }

    private static void styleVolume(Worksheet ws, int ligne, int colonne, boolean grisee) {
        var style = ws.style(ligne, colonne).format(FORMAT_VOLUME).borderStyle("thin");
        if (grisee) {
            style.fillColor(GRIS);
        }
        style.set();
    }

    /** La rangée des totaux, la ligne « base cotisation » (RG-DEC-31) et la légende. */
    private static void pied(Worksheet ws, Feuille feuille, TypeDeclarant type, int rangees) {
        int premiere = feuille.premiere();
        int total = premiere + rangees;
        ws.value(total - 1, 0, "Total");
        ws.value(total, 0, "base cotisation");
        ws.style(total - 1, 0).bold().borderStyle("thin").set();
        ws.style(total, 0).italic().set();
        Map<Integer, Boolean> grisees = new HashMap<>();
        Map<Integer, String> bases = new HashMap<>();
        feuille.colonnes().forEach((colonne, index) -> {
            grisees.put(index, colonne.griseePour(type));
            bases.put(index, colonne == ColonneDsm.C01 ? null : colonne.assiettePour(type) ? "OUI" : "NON");
        });
        if (feuille == Feuille.RECAPITULATIF) {
            List.of(6, 7).forEach(index -> {
                grisees.put(index, false);
                bases.put(index, "NON");
            });
        }
        if (feuille == Feuille.ENTREES) {
            grisees.put(10, false);
            grisees.put(11, ColonneDsm.C11.griseePour(type));
        }
        grisees.forEach((index, grisee) -> {
            String colonne = lettre(index);
            formule(ws, total - 1, index, "SUM(" + colonne + premiere + ":" + colonne + (total - 1) + ")");
            ws.style(total - 1, index).format(FORMAT_VOLUME).bold().borderStyle("thin").fillColor(grisee ? GRIS : ENTETE)
                    .set();
            String base = bases.get(index);
            if (base != null) {
                ws.value(total, index, base);
                ws.style(total, index).italic().horizontalAlignment("center").set();
            }
        });
        ws.value(total + 1, 0, "* la colonne pertes sur inventaire doit indiquer le détail du volume recensé dans la"
                + " déclaration d'inventaire annuel (DIA) déposée aux Douanes au cours de la période de référence.");
        ws.value(total + 2, 0, "Légende de la clé : M : Millésime - N : Non Millésime - V : Verre - B : sous bois");
    }

    /** Le 6e onglet, ignoré à l'import : le détail de la cotisation, ou ses factures (RG-DEC-31 à 34). */
    private static void cotisation(Worksheet ws, EditionDsm edition) {
        ws.value(0, 0, "COTISATION");
        ws.style(0, 0).bold().fontSize(14).set();
        ws.value(1, 0, "Onglet informatif : il n'est pas relu à l'import.");
        ws.style(1, 0).italic().set();
        ws.width(0, 40);
        for (int c = 1; c <= 5; c++) {
            ws.width(c, 18);
        }
        CotisationDsm cotisation = edition.cotisation();
        switch (cotisation) {
            case Calculee calculee -> {
                Detail detail = calculee.detail();
                TauxEnVigueur taux = detail.taux();
                ws.value(3, 0, "Taux CVO : " + taux.taux().montant().toPlainString().replace('.', ',') + " €/hlAP, dont "
                        + taux.taux().pourcentageExonere().toPlainString().replace('.', ',') + " % exonérés de TVA, en vigueur "
                        + taux.fin().map(f -> "du " + taux.taux().debut().format(DATE) + " au " + f.format(DATE))
                                .orElse("depuis le " + taux.taux().debut().format(DATE)));
                String[] titres = {"Ligne de cotisation", "Volume (hl.l,cl AP)", "Volume (hlAP)",
                        "Part non assujettie (€)", "Part assujettie (€)", "Total HT (€)"};
                int ligne = 5;
                titres(ws, ligne, titres);
                for (LigneDetail l : detail.lignes()) {
                    ligne++;
                    ws.value(ligne, 0, l.ligne().libelle());
                    nombre(ws, ligne, 1, l.volume().litres(), FORMAT_VOLUME);
                    nombre(ws, ligne, 2, l.hectolitres(), "0.0000");
                    nombre(ws, ligne, 3, l.nonAssujettie(), FORMAT_EUROS);
                    nombre(ws, ligne, 4, l.assujettie(), FORMAT_EUROS);
                    nombre(ws, ligne, 5, l.total(), FORMAT_EUROS);
                }
                ligne++;
                ws.value(ligne, 0, "Total");
                ws.style(ligne, 0).bold().set();
                nombre(ws, ligne, 3, detail.totalNonAssujettie(), FORMAT_EUROS);
                nombre(ws, ligne, 4, detail.totalAssujettie(), FORMAT_EUROS);
                nombre(ws, ligne, 5, detail.totalHt(), FORMAT_EUROS);
                ligne += 2;
                if (calculee.tva().isPresent()) {
                    ws.value(ligne, 0, "TVA indicative (" + calculee.tva().get().taux().toPlainString().replace('.', ',')
                            + " % de la part assujettie)");
                    nombre(ws, ligne, 5, calculee.tva().get().montant(), FORMAT_EUROS);
                    ws.value(ligne + 1, 0, "TTC indicatif");
                    nombre(ws, ligne + 1, 5, calculee.ttc().orElseThrow(), FORMAT_EUROS);
                } else {
                    ws.value(ligne, 0, "TVA indicative non disponible");
                }
                ws.value(ligne + 3, 0, edition.dsm().etat() == EtatDsm.VALIDEE ? "À facturer" : "Provisoire");
                ws.style(ligne + 3, 0).bold().set();
            }
            case CotisationDsm.NonCalculable n -> ws.value(3, 0, "Non calculable : aucun taux CVO n'est en vigueur au "
                    + edition.dsm().periode().fin().format(DATE) + ", fin de la période.");
            case Facturee facturee -> {
                int ligne = 3;
                titres(ws, ligne, new String[] {"Document", "Numéro", "Date", "HT (€)", "TVA (€)", "TTC (€)"});
                for (FactureDsm f : facturee.documents()) {
                    ligne++;
                    ws.value(ligne, 0, f.type() == FactureDsm.Type.FACTURE ? "Facture" : "Avoir");
                    ws.value(ligne, 1, f.numero());
                    ws.value(ligne, 2, f.date().format(DATE));
                    nombre(ws, ligne, 3, f.ht(), FORMAT_EUROS);
                    nombre(ws, ligne, 4, f.tva(), FORMAT_EUROS);
                    nombre(ws, ligne, 5, f.ttc(), FORMAT_EUROS);
                }
                ligne++;
                ws.value(ligne, 0, "Total HT facturé");
                ws.style(ligne, 0).bold().set();
                nombre(ws, ligne, 3, facturee.totalHt(), FORMAT_EUROS);
            }
        }
    }

    /**
     * fastexcel 0.19 écrit l'expression d'une formule telle quelle dans le XML de la feuille : le « & » des noms
     * d'onglets du modèle ({@code 'MVMTS SORTIES & FABRICATIONS'!E9}) doit être échappé ici.
     */
    private static void formule(Worksheet ws, int ligne, int colonne, String expression) {
        ws.formula(ligne, colonne, expression.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"));
    }

    private static void titres(Worksheet ws, int ligne, String[] titres) {
        for (int c = 0; c < titres.length; c++) {
            ws.value(ligne, c, titres[c]);
            styleEntete(ws, ligne, c, false);
        }
    }

    private static void nombre(Worksheet ws, int ligne, int colonne, BigDecimal valeur, String format) {
        ws.value(ligne, colonne, valeur);
        ws.style(ligne, colonne).format(format).set();
    }

    // --- Lecture (RG-DEC-37, 38) ---

    @Override
    public ClasseurLu lire(byte[] contenu) {
        if (!ReadableWorkbook.isOOXMLZipHeader(contenu)) {
            throw illisible();
        }
        try (ReadableWorkbook classeur = new ReadableWorkbook(new ByteArrayInputStream(contenu))) {
            List<Sheet> feuilles = classeur.getSheets().toList();
            if (feuilles.size() < Feuille.values().length) {
                throw new RegleEnfreinte("RG-DEC-37", "Le fichier a " + feuilles.size() + " onglet(s) : le modèle de DSM"
                        + " en a cinq (" + Arrays.stream(Feuille.values()).map(f -> f.nom)
                        .collect(Collectors.joining(", ")) + ").");
            }
            Map<Feuille, Map<Integer, Row>> lignes = new EnumMap<>(Feuille.class);
            Map<Feuille, Integer> premieres = new EnumMap<>(Feuille.class);
            Map<Feuille, Map<ColonneDsm, Integer>> colonnes = new EnumMap<>(Feuille.class);
            for (Feuille feuille : Feuille.values()) {
                Sheet sheet = feuilles.get(feuille.ordinal());
                Map<Integer, Row> parNumero = sheet.read().stream()
                        .collect(Collectors.toMap(Row::getRowNum, r -> r, (a, b) -> a));
                lignes.put(feuille, parNumero);
                int numeros = ligneDesNumeros(feuille, sheet.getName(), parNumero, colonnes);
                premieres.put(feuille, numeros + 1);
            }
            Map<Integer, Row> recapitulatif = lignes.get(Feuille.RECAPITULATIF);
            int premiere = premieres.get(Feuille.RECAPITULATIF);
            int derniere = recapitulatif.keySet().stream().mapToInt(Integer::intValue).max().orElse(premiere);
            List<ClasseurLu.Rangee> rangees = new ArrayList<>();
            for (int ligne = premiere; ligne <= derniere; ligne++) {
                Row row = recapitulatif.get(ligne);
                CelluleLue designation = cellule(OngletDsm.RECAPITULATIF, ligne, 0, row);
                if ("total".equalsIgnoreCase(designation.texte())) {
                    break;
                }
                int rang = ligne - premiere;
                Map<ColonneDsm, CelluleLue> volumes = new EnumMap<>(ColonneDsm.class);
                for (Feuille feuille : Feuille.values()) {
                    int numero = premieres.get(feuille) + rang;
                    Row r = lignes.get(feuille).get(numero);
                    colonnes.get(feuille).forEach((colonne, index) ->
                            volumes.put(colonne, cellule(feuille.onglet, numero, index, r)));
                }
                rangees.add(new ClasseurLu.Rangee(ligne, designation, cellule(OngletDsm.RECAPITULATIF, ligne, 1, row),
                        cellule(OngletDsm.RECAPITULATIF, ligne, 2, row), volumes));
            }
            Row lignePeriode = recapitulatif.get(LIGNE_PERIODE);
            Optional<Cell> periode = lignePeriode == null ? Optional.empty() : lignePeriode.getOptionalCell(0);
            return new ClasseurLu(cellule(OngletDsm.RECAPITULATIF, LIGNE_PERIODE, 0, lignePeriode),
                    periode.flatMap(ExcelDsm::mois), rangees);
        } catch (IOException | RuntimeException e) {
            if (e instanceof RegleEnfreinte regle) {
                throw regle;
            }
            throw illisible();
        }
    }

    private static RegleEnfreinte illisible() {
        return new RegleEnfreinte("RG-DEC-37", "Le fichier n'est pas un classeur Excel (.xlsx) lisible.");
    }

    /**
     * La ligne des numéros de colonne de l'onglet, parmi ses 30 premières : celle qui porte tous les numéros des
     * colonnes de l'onglet. Note leur position ; les colonnes reprises d'un autre onglet (11 et 12 des entrées)
     * sont ignorées.
     */
    private static int ligneDesNumeros(Feuille feuille, String nom, Map<Integer, Row> lignes,
            Map<Feuille, Map<ColonneDsm, Integer>> colonnes) {
        List<ColonneDsm> attendues = feuille.colonnes().keySet().stream().toList();
        for (int numero = 1; numero <= 30; numero++) {
            Row row = lignes.get(numero);
            if (row == null) {
                continue;
            }
            Map<ColonneDsm, Integer> trouvees = new EnumMap<>(ColonneDsm.class);
            for (Cell cell : row) {
                if (cell == null) {
                    continue;
                }
                BigDecimal valeur = cellule(feuille.onglet, numero, cell.getColumnIndex(), row).nombre();
                if (valeur != null && valeur.stripTrailingZeros().scale() <= 0 && valeur.intValue() >= 1
                        && valeur.intValue() <= 26) {
                    ColonneDsm colonne = ColonneDsm.numero(valeur.intValue());
                    if (colonne.onglet() == feuille.onglet) {
                        trouvees.putIfAbsent(colonne, cell.getColumnIndex());
                    }
                }
            }
            if (trouvees.keySet().containsAll(attendues)) {
                colonnes.put(feuille, trouvees);
                return numero;
            }
        }
        throw new RegleEnfreinte("RG-DEC-37", "L'onglet « " + nom + " » (" + feuille.onglet.libelle() + ") n'a pas la ligne"
                + " des numéros de colonne du modèle (" + attendues.stream().map(c -> String.valueOf(c.numero()))
                .collect(Collectors.joining(", ")) + ").");
    }

    /** Une cellule telle quelle ; une formule se lit par sa dernière valeur calculée, absente si jamais calculée. */
    private static CelluleLue cellule(OngletDsm onglet, int ligne, int index, Row row) {
        Optional<Cell> lue = row == null ? Optional.empty() : row.getOptionalCell(index);
        String lettre = lettre(index);
        if (lue.isEmpty() || lue.get() == null) {
            return new CelluleLue(onglet, ligne, lettre, null, null);
        }
        Cell cell = lue.get();
        return switch (cell.getType()) {
            case NUMBER -> {
                BigDecimal nombre = cell.asNumber();
                yield new CelluleLue(onglet, ligne, lettre, nombre.stripTrailingZeros().toPlainString(), nombre);
            }
            case FORMULA -> {
                String brut = cell.getRawValue();
                BigDecimal nombre = decimal(brut);
                yield new CelluleLue(onglet, ligne, lettre, nombre == null ? brut : nombre.toPlainString(), nombre);
            }
            case EMPTY -> new CelluleLue(onglet, ligne, lettre, null, null);
            default -> new CelluleLue(onglet, ligne, lettre, cell.getText(), null);
        };
    }

    private static BigDecimal decimal(String texte) {
        try {
            return texte == null ? null : new BigDecimal(texte.strip());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** La période en A8 : une date (le modèle l'écrit ainsi), ou un texte « 06/2026 », « juin 2026 »… */
    private static Optional<YearMonth> mois(Cell cell) {
        try {
            return switch (cell.getType()) {
                case NUMBER -> Optional.of(YearMonth.from(cell.asDate()));
                case FORMULA -> Optional.ofNullable(decimal(cell.getRawValue()))
                        .map(serie -> YearMonth.from(LocalDate.of(1899, 12, 30).plusDays(serie.longValue())));
                case STRING -> moisEcrit(cell.getText());
                default -> Optional.empty();
            };
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private static Optional<YearMonth> moisEcrit(String texte) {
        String t = texte.strip();
        for (DateTimeFormatter format : MOIS_LUS) {
            try {
                return Optional.of(YearMonth.parse(t.toLowerCase(Locale.FRENCH), format));
            } catch (DateTimeParseException e) {
                // format suivant
            }
        }
        try {
            return Optional.of(YearMonth.from(LocalDate.parse(t, DATE)));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    /** {@code 0} → {@code A}, {@code 26} → {@code AA}. */
    static String lettre(int index) {
        StringBuilder lettres = new StringBuilder();
        for (int i = index; i >= 0; i = i / 26 - 1) {
            lettres.insert(0, (char) ('A' + i % 26));
        }
        return lettres.toString();
    }
}
