package fr.bnia.declarations.adapter.document;

import fr.bnia.declarations.application.ConsulterStocksDeclares.LigneStockListee;
import fr.bnia.declarations.application.EditionsStocks;
import fr.bnia.declarations.application.StocksLecture.LigneStock;
import fr.bnia.declarations.domain.IdentiteLigne;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.springframework.stereotype.Component;

/**
 * L'état des stocks déclarés, pour les douanes (UC-DEC-1, RG-DEC-42, ADR-020) : une ligne par ligne de stock,
 * avec son opérateur et la fin de période de la DSM dont elle est tirée ; volumes en litres au masque
 * {@code #0\.00.00} (hl.l,cl), comme l'édition de la DSM ; total en formule.
 */
@Component
class ExcelStocks implements EditionsStocks {

    private static final String FORMAT_VOLUME = "#0\\.00.00";
    private static final String ENTETE = "DDEBF7";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] COLONNES = {"N° ressortissant", "Raison sociale", "Type de déclarant",
            "Fin de période", "Clé", "Millésime ou dénomination", "Compte d'âge", "Stock (hl.l,cl AP)"};
    /** Première ligne des données, après le titre et l'en-tête de colonnes. */
    private static final int PREMIERE = 5;

    @Override
    public byte[] excel(List<LigneStockListee> lignes, LocalDate etabliLe) {
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        try {
            Workbook classeur = new Workbook(sortie, "BNIA", "1.0");
            Worksheet ws = classeur.newWorksheet("Stocks déclarés");
            ws.value(0, 0, "BUREAU NATIONAL INTERPROFESSIONNEL DE L'ARMAGNAC");
            ws.style(0, 0).bold().set();
            ws.value(1, 0, "État des stocks déclarés, établi le " + etabliLe.format(DATE));
            ws.style(1, 0).bold().fontSize(14).set();
            ws.value(2, 0, "Stock de fin de la dernière DSM validée de chaque opérateur, en litres d'alcool pur,"
                    + " au masque hl.l,cl ; lignes au stock nul omises.");
            ws.style(2, 0).italic().set();
            for (int c = 0; c < COLONNES.length; c++) {
                ws.value(PREMIERE - 1, c, COLONNES[c]);
                ws.style(PREMIERE - 1, c).bold().fillColor(ENTETE).wrapText(true).set();
            }
            int ligne = PREMIERE;
            for (LigneStockListee listee : lignes) {
                LigneStock l = listee.ligne();
                IdentiteLigne identite = l.identite();
                ws.value(ligne, 0, listee.code());
                ws.value(ligne, 1, listee.raisonSociale());
                ws.value(ligne, 2, l.type().libelle());
                ws.value(ligne, 3, l.periode().fin());
                ws.style(ligne, 3).format("dd/mm/yyyy").set();
                ws.value(ligne, 4, identite.cle().name());
                if (identite.millesime() != null) {
                    ws.value(ligne, 5, identite.millesime().annee());
                } else {
                    ws.value(ligne, 5, identite.denomination().texte());
                }
                ws.value(ligne, 6, identite.compte().toString());
                ws.value(ligne, 7, l.stock().litres());
                ws.style(ligne, 7).format(FORMAT_VOLUME).set();
                ligne++;
            }
            ws.value(ligne, 0, "Total");
            ws.style(ligne, 0).bold().set();
            if (ligne > PREMIERE) {
                ws.formula(ligne, 7, "SUM(H" + (PREMIERE + 1) + ":H" + ligne + ")");
            } else {
                ws.value(ligne, 7, 0);
            }
            ws.style(ligne, 7).bold().format(FORMAT_VOLUME).set();
            ws.width(0, 16);
            ws.width(1, 34);
            ws.width(2, 16);
            ws.width(3, 14);
            ws.width(4, 8);
            ws.width(5, 28);
            ws.width(6, 13);
            ws.width(7, 18);
            ws.freezePane(0, PREMIERE);
            classeur.finish();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return sortie.toByteArray();
    }
}
