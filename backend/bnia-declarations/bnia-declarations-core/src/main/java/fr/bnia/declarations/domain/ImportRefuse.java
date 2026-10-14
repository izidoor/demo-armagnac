package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.util.List;

/** RG-DEC-37 : l'import est tout ou rien ; le fichier est refusé avec la liste de ses erreurs, situées. */
public class ImportRefuse extends RegleEnfreinte {

    /** Une erreur du fichier, à sa place dans le classeur. */
    public record Erreur(OngletDsm onglet, int ligne, String colonne, String message) {

        static Erreur sur(CelluleLue cellule, String message) {
            return new Erreur(cellule.onglet(), cellule.ligne(), cellule.colonne(), message);
        }
    }

    private final transient List<Erreur> erreurs;

    public ImportRefuse(List<Erreur> erreurs) {
        super("RG-DEC-37", "Le fichier est refusé (" + erreurs.size() + (erreurs.size() > 1 ? " erreurs" : " erreur")
                + ") : rien n'est importé.");
        this.erreurs = List.copyOf(erreurs);
    }

    public List<Erreur> erreurs() {
        return erreurs;
    }
}
