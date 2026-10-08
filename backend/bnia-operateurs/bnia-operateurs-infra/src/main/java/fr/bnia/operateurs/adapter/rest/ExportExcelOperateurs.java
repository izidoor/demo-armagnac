package fr.bnia.operateurs.adapter.rest;

import fr.bnia.operateurs.application.OperateurLigne;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Function;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;

/** RG-OPE-38 : le listing filtré au format Excel, avec les colonnes affichées à l'écran (spec 02, §6.1). */
final class ExportExcelOperateurs {

    private record Colonne(String titre, Function<OperateurLigne, String> valeur) {
    }

    private static final List<Colonne> COLONNES = List.of(
            new Colonne("Type", l -> l.type().libelle()),
            new Colonne("Code", OperateurLigne::code),
            new Colonne("Raison sociale", OperateurLigne::raisonSociale),
            new Colonne("Nom commercial", OperateurLigne::nomCommercial),
            new Colonne("Code postal", OperateurLigne::codePostal),
            new Colonne("Ville", OperateurLigne::ville),
            new Colonne("Téléphone 1", OperateurLigne::telephone1),
            new Colonne("E-mail général", OperateurLigne::emailGeneral));

    private ExportExcelOperateurs() {
    }

    static byte[] classeur(List<OperateurLigne> lignes) {
        var sortie = new ByteArrayOutputStream();
        try {
            Workbook classeur = new Workbook(sortie, "BNIA", "1.0");
            Worksheet feuille = classeur.newWorksheet("Opérateurs");
            for (int c = 0; c < COLONNES.size(); c++) {
                feuille.value(0, c, COLONNES.get(c).titre());
                feuille.style(0, c).bold().set();
            }
            for (int l = 0; l < lignes.size(); l++) {
                for (int c = 0; c < COLONNES.size(); c++) {
                    String valeur = COLONNES.get(c).valeur().apply(lignes.get(l));
                    if (valeur != null) {
                        feuille.value(l + 1, c, valeur);
                    }
                }
            }
            feuille.freezePane(0, 1);
            classeur.finish();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return sortie.toByteArray();
    }
}
